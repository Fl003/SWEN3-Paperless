import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { fetchWithAuth } from '../services/api.js'
import DocumentTypeIcon from "../shared/DocumentTypeIcon.jsx";

export default function DocumentDetails() {
    const { id } = useParams()
    const [doc, setDoc] = useState(null)
    const [error, setError] = useState(null)

    useEffect(() => {
        const result = fetchWithAuth("/api/v1/documents/" + id);
        result.then(setDoc)
            .catch(err => setError(err.message))
    }, [id])

    const formatFileSize = (bytes) => {
        if (!bytes) return '0KB';

        const kb = bytes / 1024;
        if (kb < 1024) {
            return `${Math.round(kb)}KB`;
        }

        const mb = kb / 1024;
        return `${mb.toFixed(2)}MB`;
    };


    if (error) return <p className="muted">{error === '404' ? 'Document not found.' : `Failed to load: ${error}`}</p>
    if (!doc) return <p className="muted">Loading…</p>

    return (
        <div>
            <div class="document-header">
                <div className="document-type-title">
                    <DocumentTypeIcon contentType={doc.contentType}/>
                    <h1>{doc.name ?? '(untitled)'}</h1>
                </div>
                <Link to=".." className="btn">← Back</Link>
            </div>

            <p>
                <b>ID:</b> {doc.documentId}
            </p>
            <p>
                <b>Size:</b> {formatFileSize(doc.sizeBytes)}
            </p>
            <p>
                <b>Created:</b> {doc.createdAt ? new Date(doc.createdAt).toLocaleString('de-DE', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric'
                })
                : '-'}
            </p>
            <p>
                <b>Last Modified:</b> {doc.lastEdited ? new Date(doc.lastEdited).toLocaleString('de-DE', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric'
                })
                : '-'}
            </p>
            <p>
                <b>Tags:</b> {(doc.tags || []).join(', ')}
            </p>
            <p>
                <b>Status:</b> {doc.status}
            </p>
            <pre>
                {JSON.stringify(doc, null, 2)}
            </pre>
        </div>
    )
}