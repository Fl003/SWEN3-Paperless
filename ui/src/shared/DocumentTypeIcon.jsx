import React from 'react'

// ICONS
import DefaultIcon from "../icons/others.svg";
import ImageIcon from "../icons/image.svg";
import AudioIcon from "../icons/music.svg";
import VideoIcon from "../icons/video.svg";
import DocumentIcon from "../icons/word.svg";
import SpreadsheetIcon from "../icons/excel.svg";
import PresentationIcon from "../icons/powerpoint.svg";
import PdfIcon from "../icons/pdf.svg";
import CodeIcon from "../icons/code.svg";
import ArchiveIcon from "../icons/zip.svg";

export default function DocumentTypeIcon({ contentType }) {
    const getFileIcon = (mimeType) => {
        // Wenn kein MIME-Type vorhanden ist
        if (!mimeType) return DefaultIcon;

        // Teile den MIME-Type in Haupttyp und Subtyp
        const mainType = mimeType.split('/')[0];

        // Prüfe zuerst die Haupttypen
        switch (mainType) {
            case 'image':
                return ImageIcon;
            case 'audio':
                return AudioIcon;
            case 'video':
                return VideoIcon;
        }

        // Für spezifische MIME-Types
        switch (mimeType) {
            // Dokumente
            case 'application/msword':
            case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
            case 'application/vnd.oasis.opendocument.text':
            case 'text/plain':
            case 'text/markdown':
                return DocumentIcon;

            // Tabellen
            case 'application/vnd.ms-excel':
            case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
            case 'application/vnd.oasis.opendocument.spreadsheet':
            case 'text/csv':
                return SpreadsheetIcon;

            // Präsentationen
            case 'application/vnd.ms-powerpoint':
            case 'application/vnd.openxmlformats-officedocument.presentationml.presentation':
            case 'application/vnd.oasis.opendocument.presentation':
                return PresentationIcon;

            // PDF
            case 'application/pdf':
                return PdfIcon;

            // Code
            case 'text/javascript':
            case 'application/json':
            case 'text/html':
            case 'text/css':
            case 'text/x-java':
            case 'text/x-python':
                return CodeIcon;

            // Archive
            case 'application/zip':
            case 'application/x-rar-compressed':
            case 'application/x-7z-compressed':
            case 'application/x-zip-compressed':
            case 'application/gzip':
                return ArchiveIcon;

            default:
                return DefaultIcon;
        }
    };

    return (
        <img src={getFileIcon(contentType)} alt="File icon" className="file-icon"/>
    )
}