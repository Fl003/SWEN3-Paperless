import React from 'react'
import { Link } from 'react-router-dom'
import DeleteIcon from '../icons/delete.svg'
import ArrowIcon from '../icons/arrow.svg'
import DocumentTypeIcon from "../shared/DocumentTypeIcon.jsx";

export default function DocumentsListView({ docs, deleteFile }) {
    return (
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Title / Filename</th>
                <th>Tags</th>
                <th>Created</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            {docs.map(d => (
                <tr key={d.documentId}>
                    <td>{d.documentId ?? '—'}</td>
                    <td className="title">
                        <DocumentTypeIcon contentType={d.contentType}/>
                        <Link to={`/document/${encodeURIComponent(d.documentId)}`}>
                            {d.name ?? '(untitled)'}
                        </Link>
                    </td>
                    <td>{(d.tags || []).join(', ')}</td>
                    <td>
                        {d.createdAt ? new Date(d.createdAt).toLocaleString('de-DE', {
                                day: 'numeric',
                                month: 'short',
                                year: 'numeric'
                            })
                            : '—'}
                    </td>
                    <td className="actions">
                        <button onClick={(e) => { e.preventDefault(); deleteFile(d.documentId)}}>
                            <img src={DeleteIcon} alt="Delete"/>
                        </button>
                        <Link to={`/document/${encodeURIComponent(d.documentId)}`}>
                            <img src={ArrowIcon} alt="Details"/>
                        </Link>
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    )
}