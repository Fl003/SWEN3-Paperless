async function loadDocuments() {
    const status = document.getElementById('status');
    const table = document.getElementById('docsTable');
    const body = document.getElementById('docsBody');

    try {
        // nginx will proxy this to the backend
        const res = await fetch('/api/v1/documents');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const docs = await res.json();

        body.innerHTML = '';
        (docs || []).forEach(doc => {
            const tr = document.createElement('tr');

            const tdId = document.createElement('td');
            tdId.textContent = doc.id ?? '—';

            const tdName = document.createElement('td');
            const link = document.createElement('a');
            link.href = `./details.html?id=${encodeURIComponent(doc.id)}`;
            link.textContent = doc.title ?? doc.filename ?? '(untitled)';
            tdName.appendChild(link);

            const tdTags = document.createElement('td');
            tdTags.textContent = (doc.tags || []).join(', ');

            const tdCreated = document.createElement('td');
            tdCreated.textContent = doc.createdAt ?? doc.created ?? '—';

            tr.append(tdId, tdName, tdTags, tdCreated);
            body.appendChild(tr);
        });

        status.classList.add('hidden'); table.classList.remove('hidden');
    } catch (err) {
        status.textContent = `Failed to load: ${err.message}`;
    }
}
document.addEventListener('DOMContentLoaded', loadDocuments);
