import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listDocuments } from '../services/api.js'
import AddDocumentModal from '../shared/AddDocumentModal.jsx'

export default function Documents() {
    const [docs, setDocs] = useState(null)
    const [error, setError] = useState(null)
    const [showAdd, setShowAdd] = useState(false)

    async function refresh() {
        try {
            setError(null)
            const data = await listDocuments()
            setDocs(data)
        } catch (err) {
            setError(err.message)
        }
    }

    useEffect(() => { refresh() }, [])

    return (
        <>
            {error && <p className="muted">Failed to load: {error}</p>}
            {!docs && !error && <p className="muted">Loading…</p>}

            {docs && (
                <div className="card">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Title / Filename</th>
                            <th>Tags</th>
                            <th>Created</th>
                        </tr>
                        </thead>
                        <tbody>
                        {docs.map(d => (
                            <tr key={d.documentId}>
                                <td>{d.documentId ?? '—'}</td>
                                <td>
                                    <Link to={`/document/${encodeURIComponent(d.documentId)}`}>
                                        {d.name ?? '(untitled)'}
                                    </Link>
                                </td>
                                <td>{(d.tags || []).join(', ')}</td>
                                <td>{d.createdAt ?? '—'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
            <div className="actions-center">
                <button className="btn btn-primary" onClick={() => setShowAdd(true)}>
                    + Add document
                </button>
            </div>

            {showAdd && (
                <AddDocumentModal
                    onClose={() => setShowAdd(false)}
                    onCreated={() => { setShowAdd(false); refresh() }}
                />
            )}
        </>
    )
}