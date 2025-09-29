import React, { useState } from 'react'
import {fetchWithAuth} from "../services/api.js";

export default function DeleteDocumentModal({ fileId, title, onClose, onDeleted }) {
    const [busy, setBusy] = useState(false)
    const [err, setErr] = useState(null)

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
        setErr(v);
        if (v) return;

        try {
            const response = await fetchWithAuth('/api/v1/documents/' + fileId, { method: 'DELETE' });
            console.log(response);
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