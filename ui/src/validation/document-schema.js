
/**
 * Syntactic validation: shape & formats
 * Semantic-lite: simple constraints we can check client-side (e.g., > 0)
 * Cross-field: place here if you add relationships later
 */
const MIME_REGEX = /^[a-z0-9!#$&^_.+-]{1,127}\/[a-z0-9!#$&^_.+-]{1,127}$/i;

export function validateDocumentInput({ name, contentType, sizeBytes, tags }) {
    const errors = {};

    // name-required, trimmed, length
    if (!name || !name.trim()) {
        errors.name = 'Name is required.';
    } else if (name.trim().length > 255) {
        errors.name = 'Name must be ≤ 255 characters.';
    }

    // contentType-required, mime-like
    if (!contentType || !contentType.trim()) {
        errors.contentType = 'Content type is required.';
    } else if (!MIME_REGEX.test(contentType.trim())) {
        errors.contentType = 'Use a valid MIME type (e.g., application/pdf).';
    }

    // sizeBytes-required, integer ≥ 0
    if (sizeBytes === '' || sizeBytes === null || sizeBytes === undefined) {
        errors.sizeBytes = 'Size (bytes) is required.';
    } else if (!/^\d+$/.test(String(sizeBytes))) {
        errors.sizeBytes = 'Size must be an integer number.';
    } else if (Number(sizeBytes) < 0) {
        errors.sizeBytes = 'Size must be ≥ 0.';
    } else if (Number(sizeBytes) > 2_147_483_647 * 10) {
        errors.sizeBytes = 'Size looks unrealistic.';
    }

    // tags- , separated; each ≤ 50 chars
    if (tags && typeof tags === 'string') {
        const arr = tags.split(',').map(t => t.trim()).filter(Boolean);
        const tooLong = arr.find(t => t.length > 50);
        if (tooLong) errors.tags = 'Each tag must be ≤ 50 characters.';
    }

    return errors;
}

export function toDocumentPayload({ name, contentType, sizeBytes, tags }) {
    const tagArray = Array.isArray(tags)
        ? tags
        : (tags || '')
            .split(',')
            .map(t => t.trim())
            .filter(Boolean);

    return {
        name: name?.trim() || null,
        contentType: contentType?.trim() || null,
        sizeBytes: Number(sizeBytes ?? 0),
        tags: tagArray,
    };
}
