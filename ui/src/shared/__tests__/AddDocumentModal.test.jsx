import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddDocumentModal from '../AddDocumentModal.jsx'
import { vi } from 'vitest'

function makeFileOfBytes(n, name = 'file.bin', type = 'application/octet-stream') {
    // make a file with n bytes
    const bytes = new Uint8Array(n)
    return new File([bytes], name, { type })
}

describe('AddDocumentModal', () => {
    beforeEach(() => {
        vi.restoreAllMocks()
        // default mock: ok response with JSON
        global.fetch = vi.fn(async () => ({
            ok: true,
            status: 201,
            json: async () => ({ documentId: 42 })
        }))
    })

    test('submitting without a file shows validation error', async () => {
        render(<AddDocumentModal onClose={() => {}} onCreated={() => {}} />)

        const save = screen.getByRole('button', { name: /save/i })
        userEvent.click(save)

        // error text shown below file input (exact phrasing can vary; match broadly)
        await waitFor(() => {
            expect(screen.getByText(/choose a file|file to upload/i)).toBeInTheDocument()
        })

        expect(global.fetch).not.toHaveBeenCalled()
    })

    test('selecting a too-large file shows size error before submit', async () => {
        render(<AddDocumentModal onClose={() => {}} onCreated={() => {}} />)

        const input = screen.getByLabelText(/drag & drop|choose a file/i, { selector: 'input[type="file"]' })
            || screen.getByRole('textbox', { hidden: true }) // fallback if label lookup differs

        // 10MB + 1 byte to trigger limit
        const huge = makeFileOfBytes(10 * 1024 * 1024 + 1, 'huge.pdf', 'application/pdf')
        await waitFor(() => {
            fireEvent.change(input, { target: { files: [huge] } })
        })

        // allow either wording from your component
        expect(screen.getByText(/10\s*mb/i)).toBeInTheDocument()
        expect(global.fetch).not.toHaveBeenCalled()
    })

    test('valid file + tags submits FormData and calls onCreated & onClose', async () => {
        const onCreated = vi.fn()
        const onClose = vi.fn()
        render(<AddDocumentModal onClose={onClose} onCreated={onCreated} />)

        const fileInput = screen.getByLabelText(/drag & drop|choose a file/i, { selector: 'input[type="file"]' })
            || screen.getByRole('textbox', { hidden: true })

        const okFile = makeFileOfBytes(1234, 'doc.pdf', 'application/pdf')
        fireEvent.change(fileInput, { target: { files: [okFile] } })

        const tagsInput = screen.getByLabelText(/tags/i)
        await userEvent.clear(tagsInput)
        await userEvent.type(tagsInput, ' a , , b ,c  ')

        const save = screen.getByRole('button', { name: /save/i })
        userEvent.click(save)

        await waitFor(() => expect(global.fetch).toHaveBeenCalledTimes(1))

        const [url, opts] = global.fetch.mock.calls[0]
        expect(url).toBe('/api/v1/documents')
        expect(opts.method).toBe('POST')
        expect(opts.body).toBeInstanceOf(FormData)

        //check that FormData contains our file and normalized tags
        const body = opts.body
        const sentFile = body.get('file')
        const sentTags = body.get('tags')

        expect(sentFile).toBeInstanceOf(File)
        expect(sentFile.name).toBe('doc.pdf')

        const parsedTags = JSON.parse(sentTags)
        expect(parsedTags).toEqual(['a', 'b', 'c'])

        // success callbacks
        await waitFor(() => {
            expect(onCreated).toHaveBeenCalled()
            expect(onClose).toHaveBeenCalled()
        })
    })

    test('server failure shows top-level error', async () => {
        global.fetch = vi.fn(async () => ({
            ok: false,
            status: 400,
            text: async () => 'bad input'
        }))

        render(<AddDocumentModal onClose={() => {}} onCreated={() => {}} />)

        const fileInput = screen.getByLabelText(/drag & drop|choose a file/i, { selector: 'input[type="file"]' })
            || screen.getByRole('textbox', { hidden: true })

        const okFile = makeFileOfBytes(1024, 'doc.pdf', 'application/pdf')
        fireEvent.change(fileInput, { target: { files: [okFile] } })

        const save = screen.getByRole('button', { name: /save/i })
        userEvent.click(save)

        await waitFor(() => {
            expect(screen.getByText(/http 400/i)).toBeInTheDocument()
        })
    })
})
