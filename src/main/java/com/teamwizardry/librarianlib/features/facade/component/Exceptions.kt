package com.teamwizardry.librarianlib.features.facade.component

class LayerHierarchyException: RuntimeException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}
