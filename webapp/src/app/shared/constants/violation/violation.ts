const keys = {
    date: 'Date',
    reason: 'Reason',
    source: 'Source',
    status: 'Status',
    expirydate: 'Expiry Date',
};
const columns = [keys.date, keys.reason, keys.source, keys.status, keys.expirydate];

export const VIOLATION = {
    AUDIT_LOG: {
        TITLE: 'Violation Audit Logs',
        COLUMNS_KEYS: keys,
        WHITE_LISTED_COLUMNS: columns,
        WHITE_LISTED_COLUMNS_WIDTHS: Object.fromEntries(
            columns.map((key) => [key, key === keys.reason ? 2 : 1]),
        ),
    },
};
