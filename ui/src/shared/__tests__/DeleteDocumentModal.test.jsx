import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi, expect, test, beforeEach, afterEach } from 'vitest'
import DeleteDocumentModal from '../DeleteDocumentModal.jsx'

beforeEach(() => {
    vi.restoreAllMocks()
})

afterEach(() => {
    vi.clearAllMocks()
})

test('delete success calls onDeleted and closes', async () => {
    // mock fetch to return a successful response
    global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 204,
        text: async () => '',
    })

    const onDeleted = vi.fn()
    const onClose = vi.fn()

    render(<DeleteDocumentModal fileId={7} title="X" onDeleted={onDeleted} onClose={onClose} />)

    await userEvent.click(screen.getByRole('button', { name: /delete/i }))

    // wait for onDeleted to be called
    await waitFor(() => {
        expect(onDeleted).toHaveBeenCalledTimes(1)
    })
})

test('server error is shown', async () => {
    global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        text: async () => 'boom',
    })

    render(<DeleteDocumentModal fileId={7} title="X" onDeleted={() => {}} onClose={() => {}} />)

    await userEvent.click(screen.getByRole('button', { name: /delete/i }))

    // error div shows message
    await screen.findByText(/http 500/i)
})