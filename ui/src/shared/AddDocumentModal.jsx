import React, { useState } from 'react'

export default function AddDocumentModal({ onClose, onCreated }) {
    const [name, setName] = useState('')
    const [contentType, setContentType] = useState('')
    const [sizeBytes, setSizeBytes] = useState('')
    const [tags, setTags] = useState('')
    const [busy, setBusy] = useState(false)
    const [err, setErr] = useState(null)

    async function submit(e) {
        e.preventDefault()
        setBusy(true); setErr(null)
        try {
            const res = await fetch('/api/v1/documents', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: name || null,
                    contentType: contentType || null,
                    sizeBytes: sizeBytes ? Number(sizeBytes) : 0,
                    tags: tags ? tags.split(',').map(t => t.trim()).filter(Boolean) : []
                })
            })
            if (!res.ok) throw new Error(`HTTP ${res.status}`)
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

                <form onSubmit={submit} className="form-grid">
                    <label>
                        <span>Name</span>
                        <input
                            required
                            value={name}
                            onChange={e => setName(e.target.value)}
                        />
                    </label>

                    <label>
                        <span>Content type</span>
                        <input
                            required
                            value={contentType}
                            onChange={e => setContentType(e.target.value)}
                        />
                    </label>

                    <label>
                        <span>Size (bytes)</span>
                        <input
                            type="number"
                            min="0"
                            required
                            value={sizeBytes}
                            onChange={e => setSizeBytes(e.target.value)}
                        />
                    </label>

                    <label>
                        <span>Tags (comma separated)</span>
                        <input
                            value={tags}
                            onChange={e => setTags(e.target.value)}
                        />
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