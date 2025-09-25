import React, { useState } from 'react'
import UploadIcon from '../icons/upload.svg'
import DocumentTypeIcon from "./DocumentTypeIcon.jsx";

// Maximale Dateigröße (in Bytes) - hier 10MB
const MAX_FILE_SIZE = 10 * 1024 * 1024;

export default function AddDocumentModal({ onClose, onCreated }) {
    const [file, setFile] = useState(null)
    const [tags, setTags] = useState('')
    const [fileOver, setFileOver] = useState(false)

    const [busy, setBusy] = useState(false)
    const [err, setErr] = useState(null)

    const handleFileChange = (e) => {
        setErr(''); // Fehler zurücksetzen

        const selectedFile = e.target.files?.[0];
        if (!selectedFile) {
            return;
        }

        // Größenüberprüfung
        if (selectedFile.size > MAX_FILE_SIZE) {
            setErr('Die Datei ist zu groß. Maximale Größe ist 10MB.');
            e.target.value = ''; // Input zurücksetzen
            return;
        }

        setFile(selectedFile);
    };

    async function submit(e) {
        e.preventDefault()
        if (!file) return;

        setBusy(true);
        setErr(null);

        const formData = new FormData();
        formData.append('file', file);

        const tagArray = tags ? tags.split(",").map(t => t.trim()).filter(Boolean) : [];
        formData.append("tags", JSON.stringify(tagArray));

        console.log(formData);

        try {
            const response = await fetch('/api/v1/documents', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${await response.text()}`);
            }

            const data = await response.json();
            console.log('Upload erfolgreich:', data);
            onCreated?.();
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

                    <div className={file || fileOver ? "active input-wrapper" : "input-wrapper"}>
                        <input type="file" onChange={handleFileChange} onDragOver={e => { e.preventDefault(); setFileOver(true) }} onDragExit={e => { e.preventDefault(); setFileOver(false) }}/>
                        {!file && (
                            <label htmlFor="file">
                                <img src={UploadIcon} alt="Upload" />
                                <span>Drag & Drop or Choose file to upload</span>
                            </label>
                        )}
                        {file && (
                            <div className="file-info">
                                <DocumentTypeIcon contentType={file.type}/>
                                <p>{file.name ?? '(untitled)'}</p>
                            </div>
                        )}
                    </div>

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