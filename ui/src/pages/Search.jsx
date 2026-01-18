// ui/src/pages/Search.jsx
import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { searchDocuments } from "../services/api";

export default function Search() {
    const navigate = useNavigate();
    const [params, setParams] = useSearchParams();

    const initialQ = params.get("q") ?? "";
    const [q, setQ] = useState(initialQ);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [results, setResults] = useState([]);

    const hasQuery = useMemo(() => (q ?? "").trim().length > 0, [q]);

    async function runSearch(queryText) {
        const trimmed = (queryText ?? "").trim();
        if (!trimmed) {
            setResults([]);
            setError("");
            return;
        }

        setLoading(true);
        setError("");

        try {
            const data = await searchDocuments(trimmed, 0, 20);
            setResults(Array.isArray(data) ? data : data?.items ?? []);
        } catch (e) {
            setResults([]);
            setError(e?.message ?? "Search failed");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        runSearch(q);
    }, [q])

    // If someone opens /search?q=something directly
    useEffect(() => {
        if (initialQ && initialQ !== q) setQ(initialQ);
        if (initialQ) runSearch(initialQ);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const showEmptyState = !loading && !error && results.length === 0 && hasQuery;

    return (
        <div className="searchPage">
            <header className="searchHeader">
                <div className="searchHeaderInner">
                    <div className="searchHeaderStack">
                        <button
                            type="button"
                            className="paperlessLogo"
                            onClick={() => navigate("/")}
                            aria-label="Go to homepage"
                            title="Go to homepage"
                        >
                            PAPERLESS
                        </button>

                        <form className="searchBar" role="search">
                            <input
                                value={q}
                                onChange={(e) => setQ(e.target.value)}
                                placeholder="Search your documents…"
                                aria-label="Search"
                            />
                        </form>
                    </div>
                </div>

                <div className="searchSubline">
                    {hasQuery && !loading && !error && (
                        <span className="muted">
              {results.length} result{results.length === 1 ? "" : "s"} for{" "}
                            <span className="searchQuery">“{q.trim()}”</span>
            </span>
                    )}
                </div>
            </header>

            <main className="searchBody">
                {error && <div className="searchError">{error}</div>}
                {showEmptyState && <div className="searchInfo">No results.</div>}

                <div className="searchResults">
                    {results.map((r) => {
                        const id = r.documentId ?? r.id; // API may use documentId
                        const key = id ?? `${r.name}-${Math.random()}`;

                        return (
                            <div className="searchResult" key={key}>
                                <div className="searchResultTop">
                                    <Link
                                        className="searchResultTitle"
                                        to={`/document/${encodeURIComponent(id)}`}
                                    >
                                        {r.name ?? "(untitled)"}
                                    </Link>
                                </div>

                                {r.snippet && (
                                    <div className="searchResultSnippet">{r.snippet}</div>
                                )}

                                <div className="searchResultMeta">
                                    {(r.tags || []).slice(0, 8).map((t) => (
                                        <span className="pill" key={t}>
                      {t}
                    </span>
                                    ))}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </main>
        </div>
    );
}