import React from 'react'
import { Link } from 'react-router-dom'
import DeleteIcon from '../icons/delete.svg'
import DocumentTypeIcon from "../shared/DocumentTypeIcon.jsx";

export default function DocumentsCardsView({ docs, deleteFile }) {
    return (
        <div className="cardsWrapper">
            {docs.map(d => (
                <Link to={`/document/${encodeURIComponent(d.documentId)}`} className="card">
                    <DocumentTypeIcon contentType={d.contentType}/>
                    <p className="title">{d.name ?? '(untitled)'}</p>
                    <p>
                        {d.createdAt ? new Date(d.createdAt).toLocaleString('de-DE', {
                            day: 'numeric',
                            month: 'short',
                            year: 'numeric'
                        })
                        : '—'}
                    </p>
                    <div className="actions">
                        <button onClick={(e) => { e.preventDefault(); deleteFile(d.documentId)}}>
                            <img src={DeleteIcon} alt="Delete"/>
                        </button>
                    </div>
                </Link>
            ))}
        </div>
    )
}