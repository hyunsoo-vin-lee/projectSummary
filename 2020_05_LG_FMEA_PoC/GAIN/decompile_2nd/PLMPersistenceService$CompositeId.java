package com.dassault_systemes.sfy.persistence;

public class PLMPersistenceService$CompositeId {
	protected static final String separator = "/";
	protected String representationId;
	protected String fileName;
	protected String objectId;
	PLMPersistenceService this$0;

	public PLMPersistenceService$CompositeId(PLMPersistenceService var1, String var2, String var3, String var4) {
		this.this$0 = var1;
		this.representationId = var2;
		this.fileName = var3;
		this.objectId = var4;
	}

	public PLMPersistenceService$CompositeId(PLMPersistenceService var1, String var2) {
		this.this$0 = var1;
		if (var2 != null) {
			String[] var3 = var2.split("/");
			if (var3.length == 3) {
				this.representationId = var3[0];
				this.fileName = var3[1];
				this.objectId = var3[2];
			} else {
				this.objectId = var2;
			}
		}

	}

	public boolean isComposite() {
		return this.representationId != null;
	}

	public String getRepresentationId() {
		return this.representationId;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getObjectId() {
		return this.objectId;
	}

	public String toString() {
		return this.representationId + "/" + this.fileName + "/" + this.objectId;
	}
}