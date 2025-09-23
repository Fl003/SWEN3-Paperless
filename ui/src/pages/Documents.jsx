import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listDocuments } from '../services/api.js'

export default function Documents() {
    const [docs, setDocs] = useState(null)
    const [error, setError] = useState(null)

    useEffect(() => {
        listDocuments()
            .then(setDocs)
            .catch(err => setError(err.message))
    }, [])

    if (error) return <p className="muted">Failed to load: {error}</p>
    if (!docs) return <p className="muted">Loading…</p>

    return (
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
    )
}