// ui/src/shared/AddDocumentModal.jsx
import React, { useState } from 'react'
import { validateDocumentInput, toDocumentPayload } from '../validation/document-schema.js'
import UploadIcon from '../icons/upload.svg'
import DocumentTypeIcon from "./DocumentTypeIcon.jsx";

// Maximale Dateigröße (in Bytes) - hier 10MB
const MAX_FILE_SIZE = 10 * 1024 * 1024;
//for tags
const MAX_TAGS = 20;
const MAX_TAG_LEN = 50;

export default function AddDocumentModal({ onClose, onCreated }) {
    const [file, setFile] = useState(null)
    const [tags, setTags] = useState('')
    const [fileOver, setFileOver] = useState(false)

    const [busy, setBusy] = useState(false)
    const [err, setErr] = useState(null)
    const [fieldErr, setFieldErr] = useState({});  // per-field validation errors

    function validate(currentFile, currentTags) {
        const fe = {};

        if (!currentFile) {
            fe.file = 'Please choose a file to upload.';
        } else {
            if (currentFile.size > MAX_FILE_SIZE) {
                fe.file = 'File is too large (max 10 MB).';
            }
            if (currentFile.size <= 0) {
                fe.file = 'File is empty.';
            }
        }

        if (typeof currentTags === 'string' && currentTags.trim().length) {
            const arr = currentTags
                .split(',')
                .map(t => t.trim())
                .filter(Boolean);

            if (arr.length > MAX_TAGS) {
                fe.tags = `Too many tags (max ${MAX_TAGS}).`;
            } else if (arr.some(t => t.length > MAX_TAG_LEN)) {
                fe.tags = `Each tag must be ≤ ${MAX_TAG_LEN} characters.`;
            }
        }

        return fe;
    }

    const handleFileChange = (e) => {
        setErr(null);
        setFieldErr({ ...fieldErr, file: undefined });

        const selectedFile = e.target.files?.[0];
        if (!selectedFile) {
            return;
        }

        // Größenüberprüfung
        if (selectedFile.size > MAX_FILE_SIZE) {
            setFieldErr(prev => ({ ...prev, file: 'File is too big, max 10 MB.' }));
            e.target.value = ''; // Input zurücksetzen
            setFile(null);
            return;
        }

        setFile(selectedFile);
    };

    async function submit(e) {
        e.preventDefault();
        setErr(null);

        // run validation
        const fe = validate(file, tags);
        setFieldErr(fe);
        if (Object.keys(fe).length) return;

        setBusy(true);

        try {
            const formData = new FormData();
            formData.append('file', file);

            const tagArray = tags
                ? tags.split(',').map(t => t.trim()).filter(Boolean)
                : [];
            formData.append('tags', JSON.stringify(tagArray));

            const response = await fetch('/api/v1/documents', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                // surface server-provided details if available
                const text = await response.text();
                throw new Error(`HTTP ${response.status}: ${text || 'Upload failed'}`);
            }

            await response.json(); // optional: consume data
            onCreated?.();         // let parent refresh
            onClose?.();           // close modal after success (optional)
        } catch (e2) {
            setErr(e2.message || 'Upload failed');
        } finally {
            setBusy(false);
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
                    {/* File input */}
                    <div
                        className={(file || fileOver ? 'active ' : '') + 'input-wrapper'}
                        onDragOver={e => { e.preventDefault(); if (!fileOver) setFileOver(true); }}
                        onDragLeave={e => { e.preventDefault(); if (fileOver) setFileOver(false); }}
                    >
                        <input
                            id="file"
                            type="file"
                            accept="*/*"
                            onChange={handleFileChange}
                            aria-invalid={Boolean(fieldErr.file)}
                            aria-describedby={fieldErr.file ? 'file-error' : undefined}
                        />

                        {!file && (
                            <label htmlFor="file">
                                <img src={UploadIcon} alt="Upload" />
                                <span>Drag & Drop or choose a file to upload</span>
                            </label>
                        )}

                        {file && (
                            <div className="file-info">
                                <DocumentTypeIcon contentType={file.type}/>
                                <p title={file.name}>{file.name ?? '(untitled)'}</p>
                            </div>
                        )}
                    </div>
                    {fieldErr.file && <div id="file-error" className="field-error">{fieldErr.file}</div>}

                    {/* Tags */}
                    <label htmlFor="tags">
                        <span>Tags (comma separated)</span>
                        <input
                            id="tags"
                            value={tags}
                            onChange={e => {
                                setTags(e.target.value);
                                if (fieldErr.tags) setFieldErr(prev => ({ ...prev, tags: undefined }));
                            }}
                            aria-invalid={Boolean(fieldErr.tags)}
                            aria-describedby={fieldErr.tags ? 'tags-error' : undefined}
                        />
                    </label>
                    {fieldErr.tags && <div id="tags-error" className="field-error">{fieldErr.tags}</div>}

                    {/* Actions */}
                    <div className="modal-actions">
                        <button type="button" className="btn" onClick={onClose} disabled={busy}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={busy}>
                            {busy ? 'Saving…' : 'Save'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}