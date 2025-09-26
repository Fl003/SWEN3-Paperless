import React, { useState } from 'react'

export default function DeleteDocumentModal({ fileId, title, onClose, onDeleted }) {
    const [busy, setBusy] = useState(false)
    const [err, setErr] = useState(null)
    const [fieldErr, setFieldErr] = useState(null); // confirmation error

    function validate() {
        if (!Number.isFinite(Number(fileId)) || Number(fileId) <= 0) {
            return 'Invalid document id.';
        }
        return null;
    }

    async function submit(e) {
        e.preventDefault()

        setBusy(true);
        setErr(null);

        const v = validate();
        setFieldErr(v);
        if (v) return;

        try {
            console.log('deleting: ' + fileId);
            const response = await fetch('/api/v1/documents/' + fileId, { method: 'DELETE' });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${await response.text()}`);
            }
            onDeleted?.();
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
                    <h3>Delete document</h3>
                    <button className="icon-btn" onClick={onClose} aria-label="Close">✕</button>
                </div>

                {err && <div className="alert">{err}</div>}

                <form onSubmit={submit} className="form-grid">
                    <p>Are you sure you want to delete the following document?</p>

                    <b>{title}</b>

                    <div className="modal-actions">
                        <button type="button" className="btn" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={busy}>
                            {busy ? 'Deleting…' : 'Delete'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}