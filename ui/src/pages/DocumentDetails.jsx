import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getDocument } from '../services/api.js'

export default function DocumentDetails() {
    const { id } = useParams()
    const [doc, setDoc] = useState(null)
    const [error, setError] = useState(null)

    useEffect(() => {
        getDocument(id)
            .then(setDoc)
            .catch(err => setError(err.message))
    }, [id])

    if (error) return <p className="muted">{error === '404' ? 'Document not found.' : `Failed to load: ${error}`}</p>
    if (!doc) return <p className="muted">Loading…</p>

    return (
        <div className="card">
            <p><b>ID:</b> {doc.documentId}</p>
            <p><b>Title:</b> {doc.name ?? '(untitled)'}</p>
            <p><b>Created:</b> {doc.createdAt ?? '—'}</p>
            <p><b>Tags:</b> {(doc.tags || []).join(', ')}</p>
            <pre>{JSON.stringify(doc, null, 2)}</pre>
            <p><Link to="..">← Back</Link></p>
        </div>
    )
}