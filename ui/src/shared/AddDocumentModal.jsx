// ui/src/shared/AddDocumentModal.jsx
import React, { useState } from 'react'
import { validateDocumentInput, toDocumentPayload } from '../validation/document-schema.js'

export default function AddDocumentModal({ onClose, onCreated }) {
    const [name, setName] = useState('')
    const [contentType, setContentType] = useState('')
    const [sizeBytes, setSizeBytes] = useState('')
    const [tags, setTags] = useState('')
    const [busy, setBusy] = useState(false)
    const [err, setErr] = useState(null)
    const [fieldErrors, setFieldErrors] = useState({})

    async function submit(e) {
        e.preventDefault()
        setErr(null)

        //validate
        const errors = validateDocumentInput({ name, contentType, sizeBytes, tags })
        setFieldErrors(errors)
        if (Object.keys(errors).length > 0) return

        // build payload
        const payload = toDocumentPayload({ name, contentType, sizeBytes, tags })

        // send
        setBusy(true)
        try {
            const res = await fetch('/api/v1/documents', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })
            if (!res.ok) {
                if (res.status === 409) setErr('A document with this name already exists.')
                else setErr(`Failed to save (HTTP ${res.status}).`)
                return
            }
            await res.json()
            onCreated?.()
        } catch (e2) {
            setErr(e2.message)
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className="modal" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h3>Add document</h3>
                    <button className="icon-btn" onClick={onClose} aria-label="Close">✕</button>
                </div>

                {err && <div className="alert">{err}</div>}

                <form onSubmit={submit} className="form-grid" noValidate>
                    <label>
                        <span>Name</span>
                        <input
                            value={name}
                            onChange={e => setName(e.target.value)}
                            aria-invalid={!!fieldErrors.name}
                            placeholder="report.pdf"
                            required
                        />
                        {fieldErrors.name && <small className="error">{fieldErrors.name}</small>}
                    </label>

                    <label>
                        <span>Content type</span>
                        <input
                            value={contentType}
                            onChange={e => setContentType(e.target.value)}
                            aria-invalid={!!fieldErrors.contentType}
                            placeholder="application/pdf"
                            required
                        />
                        {fieldErrors.contentType && <small className="error">{fieldErrors.contentType}</small>}
                    </label>

                    <label>
                        <span>Size (bytes)</span>
                        <input
                            type="number"
                            inputMode="numeric"
                            min="0"
                            value={sizeBytes}
                            onChange={e => setSizeBytes(e.target.value)}
                            aria-invalid={!!fieldErrors.sizeBytes}
                            placeholder="123456"
                            required
                        />
                        {fieldErrors.sizeBytes && <small className="error">{fieldErrors.sizeBytes}</small>}
                    </label>

                    <label>
                        <span>Tags (comma separated)</span>
                        <input
                            value={tags}
                            onChange={e => setTags(e.target.value)}
                            aria-invalid={!!fieldErrors.tags}
                            placeholder="finance, 2025"
                        />
                        {fieldErrors.tags && <small className="error">{fieldErrors.tags}</small>}
                    </label>

                    <div className="modal-actions">
                        <button type="button" className="btn" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={busy}>
                            {busy ? 'Saving…' : 'Save'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}