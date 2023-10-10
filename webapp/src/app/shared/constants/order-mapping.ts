// below order maps are based on priorities - lower the number, higher the priority

export const SeverityOrderMap = {
    critical: 1,
    high: 2,
    medium: 3,
    low: 4,
}

export const CategoryOrderMap = {
    security: 1,
    cost: 2,
    operations: 3,
    tagging: 4
}

export const AssociatedPolicyStatusOrderMap = {
    fail: 1,
    exempt: 2,
    pass: 3
}

export const ViolationStatusOrderMap = {
    open: 1,
    exempt: 2,
    closed: 3
}