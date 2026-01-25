import React, { useEffect, useState } from 'react'
import { Routes, Route, Link, useLocation } from 'react-router-dom'
import { NavLink } from "react-router-dom";
import DocumentsListView from './pages/DocumentsListView.jsx'
import DocumentDetails from './pages/DocumentDetails.jsx'
import ListIcon from './icons/list.svg'
import CardsIcon from './icons/cards.svg'
import UploadIcon from './icons/upload.svg'
import AddDocumentModal from "./shared/AddDocumentModal.jsx";
import DocumentsCardsView from "./pages/DocumentsCardsView.jsx";
import { fetchWithAuth } from './services/api.js'
import DeleteDocumentModal from "./shared/DeleteDocumentModal.jsx";
import LogoutButton from "./shared/LogoutButton.jsx";
import Search from "./pages/Search.jsx";

export default function App() {
    const location = useLocation();
    const [showAdd, setShowAdd] = useState(false)
    const [showDelete, setShowDelete] = useState(false)
    const [docs, setDocs] = useState(null)
    const [error, setError] = useState(null)
    const [listStyle, setListStyle] = useState("list")
    const [deleteId, setDeleteId] = useState(null)

    function deleteFile(id) {
        setDeleteId(id)
        setShowDelete(true)
    }

    async function refresh() {
        try {
            setError(null)
            const data = await fetchWithAuth("/api/v1/documents")
            console.log(data);
            setDocs(data);
        } catch (err) {
            setError(err.message)
        }
    }

    function closeDeleteModal() {
        setShowDelete(false);
        setDeleteId(null)
    }

    useEffect(() => { refresh() }, [])

    return (
        <>
            <div className="sidebar">
                <h1>PAPERLESS</h1>
                <nav>
                    <Link to="/">Documents</Link>
                    <NavLink to="/search" className="sidebar-link">
                        Search
                    </NavLink>
                </nav>
                <div className="sidebar-bottom">
                    <div className="logout-wrapper">
                        <LogoutButton />
                    </div>
                </div>
            </div>
            <div className="content-side">
                <div className="content-header">
                    <h2>
                        {location.pathname === "/search" ? "SEARCH" : "DOCUMENTS"}
                    </h2>
                    <div className="actions-bar">
                        {location.pathname === "/" && (
                            <div className="list-style-switch">
                                <button id="list" className={listStyle === 'list' ? 'active' : ''} onClick={() => setListStyle("list")}>
                                    <img src={ListIcon} alt="List"/>
                                </button>
                                <button id="cards" className={listStyle === 'cards' ? 'active' : ''} onClick={() => setListStyle("cards")}>
                                    <img src={CardsIcon} alt="Cards"/>
                                </button>
                            </div>
                        )}
                        <button className="btn btn-primary" onClick={() => setShowAdd(true)}>
                            <img src={UploadIcon} alt="Upload"/>
                            Upload
                        </button>
                    </div>
                </div>

                <main className="container">
                    {error && <p className="muted">Failed to load: {error}</p>}
                    {!docs && !error && <p className="muted">Loading…</p>}

                    <Routes>
                        <Route path="/" element={
                            docs && (
                                listStyle === "list"
                                    ? <DocumentsListView docs={docs} deleteFile={deleteFile} />
                                    : <DocumentsCardsView docs={docs} deleteFile={deleteFile} />
                            )
                        } />
                        <Route path="/document/:id" element={
                            <DocumentDetails />
                        } />
                        <Route path="/search" element={<Search />} />
                    </Routes>
                </main>
            </div>

            {showAdd && (
                <AddDocumentModal
                    onClose={() => setShowAdd(false)}
                    onCreated={() => { setShowAdd(false); refresh() }}
                />
            )}

            {showDelete && (
                <DeleteDocumentModal
                    fileId={deleteId}
                    title={docs.find(d => d.documentId === deleteId)?.name ?? '(untitled)' }
                    onClose={() => { closeDeleteModal() }}
                    onDeleted={() => { closeDeleteModal(); refresh()}}
                />
            )}
        </>
    )
}
