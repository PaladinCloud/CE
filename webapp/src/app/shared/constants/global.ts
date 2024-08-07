// === Exception Log Messages ===

export const API_RESPONSE_ERROR = 'apiResponseError';
export const JS_ERROR = 'jsError';
export const ERROR = 'error';
export const NO_DATA_AVAILABLE = 'noDataAvailable';
export const NO_POLICY_FOUND = 'noPolicyFound';

// === Common Labels ===

export const DEFAULT = 'default';
export const LEVEL_ZERO = 'level0';
export const ASSET = 'asset';
export const CATEGORY = 'category';
export const POLICY = 'policy';
export const VIOLATIONS = 'violations';
export const ASSET_TYPE_SPACE = 'asset type';
export const COMPLIANCE = 'compliance';

export const ASSET_TYPE_KEY = 'assetType';
export const ASSETS_LABEL = 'Assets';
export const POLICIES_LABEL = 'Policies';
export const CATEGORY_LABEL = 'Category';
export const COMPLIANT_LABEL = 'Compliant';
export const COMPLIANCE_LABEL = 'Compliance';
export const VIOLATIONS_LABEL = 'Violations';
export const SEVERITY_LABEL = 'Severity';
export const NON_COMPLIANT_LABEL = 'Non Compliant';

export const SECURITY_LABEL = 'Security';
export const COST_LABEL = 'Cost';
export const OPERATIONS_LABEL = 'Operations';
export const TAGGING_LABEL = 'Tagging';

export const CATEGORY_LIST = [SECURITY_LABEL, COST_LABEL, OPERATIONS_LABEL, TAGGING_LABEL];
export const SECURITY = 'security';
export const COST = 'cost';
export const OPERATIONS = 'operations';
export const TAGGING = 'tagging';

export const TABLE_DIRECTION_ASC = 'asc';
export const TABLE_DIRECTION_DESC = 'desc';

// === Common Labels for statues ===
export const SUCCESS = 'success';
export const ENABLE = 'enable';
export const DISABLE = 'disable';
export const ENABLED = 'enabled';
export const DISABLED = 'disabled';
export const CLOSED = 'closed';
export const EXTERNAL = 'external';
export const OPEN = 'open';
export const EXEMPTED = 'exempted';
export const EXEMPT = 'exempt';
export const ENFORCED = 'enforced';

// === Common Labels for graph ===
export const ALL_TIME = 'All time';
export const WAIT_FOR_DATA = 'waitForData';

export const REDIRECT_URL_KEY = 'redirectUrl';

// === OBJECT LITERAL VALUES  ===
export const STRING = 'string';

// === LITERAL VALUES  ===
export const NO_DATA = 'No Data';
export const DASH = '-';

// Make constants readonly
Object.freeze(module.exports);
