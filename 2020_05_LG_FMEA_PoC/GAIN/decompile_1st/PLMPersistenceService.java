package com.dassault_systemes.sfy.persistence;

import com.dassault_systemes.enovia.webapps.richeditor.util.IRichEditUtil;
import com.dassault_systemes.enovia.webapps.richeditor.util.RichEditFactory;
import com.dassault_systemes.enovia.webapps.richeditor.util.IRichEditUtil.Format;
import com.dassault_systemes.i3dx.lwc.LWCEvent;
import com.dassault_systemes.i3dx.lwc.LWCKweKernelObjectWrapper;
import com.dassault_systemes.i3dx.lwc.LWCServices;
import com.dassault_systemes.i3dx.lwc.LWCTemplate;
import com.dassault_systemes.i3dx.lwc.LWCTemplateFactory;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicAttributeItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicClassItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicEntityItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicFactory;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicInterfaceItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicRelationClassItf;
import com.dassault_systemes.knowledge_itfs.IKweDictionary;
import com.dassault_systemes.knowledge_itfs.IKweInstance;
import com.dassault_systemes.knowledge_itfs.IKweType;
import com.dassault_systemes.knowledge_itfs.IKweValueFactory;
import com.dassault_systemes.knowledge_itfs.IPLMKweFactory;
import com.dassault_systemes.knowledge_itfs.IPLMKweFactoryWithBLs;
import com.dassault_systemes.knowledge_itfs.KweInterfacesServices;
import com.dassault_systemes.sfy.persistence.JsonPersistenceService.Model;
import com.dassault_systemes.sfy.webservices.CreateStatement;
import com.dassault_systemes.sfy.webservices.UpdateStatement;
import com.matrixone.apps.domain.util.MqlUtil;
import java.util.ArrayList;
import java.util.Arrays;
import com.dassault_systemes.sfy.webservices.UpdateStatement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessInterface;
import matrix.db.BusinessInterfaceList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.Path;
import matrix.db.Path.Element;
import matrix.db.Policy;
import matrix.db.Relationship;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectList;
import matrix.db.Vault;
import matrix.db.Visuals;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class PLMPersistenceService {
	protected static final XLogger Logger = XLoggerFactory.getXLogger(PLMPersistenceService.class.getCanonicalName());
	private static final Pattern WordPattern = Pattern.compile("\"(\\w+)\"");
	private static final String PLM_EXTERNAL_ID = "PLMInstance.PLM_ExternalID";
	private static final String PATHS = "paths";
	private static final String ELEMENTS = "elements";
	protected static IPLMDictionaryPublicFactory factory = new IPLMDictionaryPublicFactory();
	protected static IPLMDictionaryPublicItf dictionary;
	protected IKweDictionary kweDictionary = KweInterfacesServices.getKweDictionary();
	protected IPLMKweFactory templateFactory = new LWCTemplateFactory();
	protected IKweValueFactory valueFactory = KweInterfacesServices.getKweValueFactory();
	protected IPLMKweFactoryWithBLs factoryWithBusinessLogics = KweInterfacesServices.getPLMKweFactoryWithBLs();
	protected Map<String, TPath> pathsById = new HashMap();
	protected JsonPersistenceService jsonService = new JsonPersistenceService();
	protected Set<String> jsonTypes = new HashSet();
	protected Set<String> toBeSavedJsonObjectIds = new HashSet();
	protected Map<String, String> repIdsByRootContainerId = new HashMap();
	protected Set<String> savedRepresentationPhysicalIds = new HashSet();
	protected IStreamManager streamManager;
	protected Map<String, String> relationsToRep = new HashMap();

	private PLMPersistenceService() {
		this.jsonTypes.add("TableModel");
		this.jsonTypes.add("TableColumn");
		this.jsonTypes.add("Characterizations");
		this.jsonTypes.add("PrivateData");
		this.relationsToRep.put("FailureModesEffectsAnalysis.FailureModesEffectsAnalysisTableModel",
				"FailureModesEffectsAnalysisRepInstance");
		this.relationsToRep.put("FailureModesEffectsAnalysis.FailureModesEffectsAnalysisCharacterizations",
				"FailureModesEffectsAnalysisRepInstance");
		this.relationsToRep.put("FailureModesEffectsAnalysis.FailureModesEffectsAnalysisPrivateData",
				"FailureModesEffectsAnalysisRepInstance");
	}

	public PLMPersistenceService(JsonPersistenceService var1, IStreamManager var2) {
		this.jsonTypes.add("TableModel");
		this.jsonTypes.add("TableColumn");
		this.jsonTypes.add("Characterizations");
		this.jsonTypes.add("PrivateData");
		this.relationsToRep.put("FailureModesEffectsAnalysis.FailureModesEffectsAnalysisTableModel",
				"FailureModesEffectsAnalysisRepInstance");
		this.relationsToRep.put("FailureModesEffectsAnalysis.FailureModesEffectsAnalysisCharacterizations",
				"FailureModesEffectsAnalysisRepInstance");
		this.relationsToRep.put("FailureModesEffectsAnalysis.FailureModesEffectsAnalysisPrivateData",
				"FailureModesEffectsAnalysisRepInstance");
		this.jsonService = var1;
		this.streamManager = var2;
	}

	protected static List<String> getExtensions(Context var0, String var1, Map<String, String> var2)
			throws MatrixException {
		ArrayList var3 = new ArrayList();
		Set var4 = var2.entrySet();
		Iterator var5 = var4.iterator();

		while (var5.hasNext()) {
			Entry var6 = (Entry) var5.next();
			String var7 = (String) var6.getKey();
			IPLMDictionaryPublicEntityItf var8 = getAttributeEntity(var0, var1, var7);
			if (var8 != null && var8 instanceof IPLMDictionaryPublicInterfaceItf && !var3.contains(var8.getName())) {
				var3.add(var8.getName());
			}
		}

		return var3;
	}

	protected void processCreateClass(Context var1, String var2, String var3, Map<String, String> var4,
			Map<String, Object> var5, Map<String, String> var6, String var7) throws MatrixException {
		IKweType var8 = this.kweDictionary.findType(var1, var3);
		HashMap var9 = new HashMap();
		var9.put("OperationId", this.valueFactory.createString(var1, "New"));
		var9.put("OperationDetail", this.valueFactory.createString(var1, "Create"));
		IKweInstance var10 = null;

		try {
			List var11 = getExtensions(var1, var3, var4);
			String var12 = (String) var4.get("containedBy");
			String var14;
			String var17;
			if (var8.isaKindOf("PLMCoreReference")) {
				var10 = this.factoryWithBusinessLogics.createReference(var1, this.templateFactory, var9, var3,
						(String) null, var11, (String) null);
				var10.setValue(var1, "name", var10.getValue(var1, "PLM_ExternalID"));
			} else {
				IKweInstance var13;
				IKweInstance var15;
				if (var8.isaKindOf("PLMCoreInstance")) {
					var13 = this.getKweInstanceByPhysicalId(var1, var12, var5);
					var14 = (String) var4.get("instanceOf");
					var15 = this.getKweInstanceByPhysicalId(var1, var14, var5);
					var10 = this.factoryWithBusinessLogics.createInstance(var1, this.templateFactory, var9, var3,
							(String) null, var13, var15, var11);
				} else if (var8.isaKindOf("PLMConnection")) {
					var17 = (String) var6.get(var12);
					IKweInstance var18 = this.getKweInstanceByPhysicalId(var1, var17 != null ? var17 : var12, var5);
					var9.put("IdCloningString", var18.getValue(var1, "PLM_ExternalID"));
					var10 = this.factoryWithBusinessLogics.createConnection(var1, this.templateFactory, var9, var3,
							(String) null, var11, (String) null, var18);
				} else if (var8.isaKindOf("PLMPort")) {
					var13 = this.getKweInstanceByPhysicalId(var1, var12, var5);
					var9.put("IdCloningString", var13.getValue(var1, "PLM_ExternalID"));
					var10 = this.factoryWithBusinessLogics.createPort(var1, this.templateFactory, var9, var3,
							(String) null, var11, (String) null, var13);
				} else if (var8.isaKindOf("PLMCoreRepReference")) {
					var17 = (String) var4.remove("policy");
					var10 = this.factoryWithBusinessLogics.createRepresentation(var1, this.templateFactory, var9, var3,
							(String) null, var11, var17);
				} else if (var8.isaKindOf("PLMCoreRepInstance")) {
					var13 = this.getKweInstanceByPhysicalId(var1, var12, var5);
					var14 = (String) var4.get("instanceOf");
					var15 = this.getKweInstanceByPhysicalId(var1, var14, var5);
					var10 = this.factoryWithBusinessLogics.createRepInstance(var1, this.templateFactory, var9, var3,
							(String) null, var13, var15, var11);
				} else {
					this.processCreateClassNoKwe(var1, var2, var3, var4, var5);
				}
			}

			if (var10 instanceof LWCTemplate) {
				this.applyAttributes(var1, var10, var4);
				var17 = ((LWCTemplate) var10).doCreate();
				var14 = var1.getCustomData("Change Id");
				if (var14 != null) {
					LWCServices.beginUserfact(var1, "MODIFY", "");
					LWCEvent var19 = new LWCEvent(var17, false, "Create", "");
					LWCServices.logEvents(var1, Arrays.asList(var19));
					LWCServices.endUserfact(var1);
				}

				var6.put(var2, var17);
				var5.put(var2, var10);
			}
		} catch (Exception var16) {
			Logger.error("Cannot create class {}", var3, var16);
		}

	}

	protected static String getVault(Context var0, String var1) {
		String var2 = var0.getVault().getName();

		try {
			IPLMDictionaryPublicClassItf var3 = dictionary.getClass(var0, var1);
			var2 = dictionary.retrieveVaults(var0, var3, (String) null, (List) null, (List) null);
		} catch (MatrixException var4) {
			Logger.error("Cannot determine vault for {}. Object creation will fail.", var1, var4);
		}

		return var2;
	}

	protected static Policy getPolicy(Context var0, BusinessType var1) {
		Policy var2 = null;

		try {
			Object var3 = null;
			IPLMDictionaryPublicClassItf var4 = dictionary.getClass(var0, var1.getName());
			String var5 = dictionary.computeNewPolicyToUse2(var0, var4, (List) var3);
			Logger.debug("Policy to use according to dictionary {}", var5);
			var2 = new Policy(var5);
		} catch (MatrixException var6) {
			Logger.error("Cannot get policy for type {}. Object creation will fail.", var1.getName(), var6);
		}

		return var2;
	}

	protected static String getDefaultRevision(Context var0, Policy var1) throws MatrixException {
		String var2 = null;
		var1.open(var0);
		if (var1.hasMajorSequence(var0)) {
			var2 = var1.getFirstInMajorSequence(var0);
			if (var1.hasMinorSequence(var0)) {
				var2 = var2.concat(".").concat(var1.getFirstInMinorSequence(var0));
			}
		} else if (var1.hasSequence(var0)) {
			var2 = var1.getFirstInMinorSequence(var0);
		}

		var1.close(var0);
		return var2;
	}

	protected void processCreateClassNoKwe(Context var1, String var2, String var3, Map<String, String> var4,
			Map<String, Object> var5) throws MatrixException {
		BusinessObject var6 = null;
		Vault var7 = var1.getVault();
		BusinessType var8 = new BusinessType(var3, var1.getVault());
		Policy var9 = getPolicy(var1, var8);
		String var10 = getDefaultRevision(var1, var9);
		var6 = new BusinessObject(var3, var2, var10, getVault(var1, var3), var2);
		AttributeList var11 = new AttributeList();
		BusinessInterfaceList var12 = new BusinessInterfaceList();
		Set var13 = var4.entrySet();
		Iterator var14 = var13.iterator();

		while (var14.hasNext()) {
			Entry var15 = (Entry) var14.next();
			String var16 = (String) var15.getKey();
			IPLMDictionaryPublicEntityItf var17 = getAttributeEntity(var1, var3, var16);
			if (var17 != null) {
				if (var17 instanceof IPLMDictionaryPublicInterfaceItf) {
					BusinessInterface var18 = new BusinessInterface(var17.getName(), var7);
					if (!var12.contains(var18)) {
						var12.add(var18);
					}
				}

				IPLMDictionaryPublicAttributeItf var22 = var17.getAttribute(var1, var16);
				String var19 = var22.getFullName(var1);
				if (var19.startsWith(".")) {
					var19 = var19.substring(1);
				}

				String var20 = this.normalizeAttribute(var1, (String) var15.getValue(), var22);
				Attribute var21 = new Attribute(new AttributeType(var19), var20);
				var11.add(var21);
			}
		}

		var6.create(var1, var9.getName(), (Visuals) null, var11, (String) null, (String) null, var2, var2, var12);
		var5.put(var2, var6);
	}

	protected IKweInstance getKweInstanceByPhysicalId(Context var1, String var2, Map<String, Object> var3) {
		Object var4 = var3.get(var2);
		if (var4 instanceof IKweInstance) {
			return (IKweInstance) var4;
		} else {
			LWCKweKernelObjectWrapper var5 = null;
			StringList var6 = new StringList();
			var6.add("physicalid");
			var6.add("logicalid");
			var6.add("majorid");
			var6.add("type");
			var6.add("name");
			var6.add("interface");
			var6.add("owner");
			var6.add("organization");
			var6.add("project");
			var6.add("originated");
			var6.add("modified");
			var6.add("reserved");
			var6.add("reservedby");
			var6.add("current");
			var6.add("policy");
			var6.add("description");
			var6.add("majorrevision");
			var6.add("locker");
			var6.add("ispublished");
			var6.add("isbestsofar");
			var6.add("locked");
			var6.add("policy");
			var6.add("minorrevision");
			var6.add("revision");
			var6.add("revindex");
			var6.add("cestamp");
			var6.add("updatestamp");
			var6.add("versionid");
			var6.add("attribute[].value");

			try {
				BusinessObjectWithSelectList var7 = BusinessObject.getSelectBusinessObjectData(var1, new String[]{var2},
						var6);

				for (int var8 = 0; var8 < var7.size(); ++var8) {
					BusinessObjectWithSelect var9 = (BusinessObjectWithSelect) var7.get(var8);
					LWCKweKernelObjectWrapper var10 = new LWCKweKernelObjectWrapper(var1, var9);
					var5 = var10;
				}
			} catch (Exception var11) {
				;
			}

			return var5;
		}
	}

	protected void applyAttributes(Context var1, IKweInstance var2, Map<String, String> var3) throws MatrixException {
		String var4 = var2.getType().getName();
		Set var5 = var3.entrySet();
		if (var2 instanceof LWCTemplate) {
			Iterator var6 = var5.iterator();

			while (var6.hasNext()) {
				Entry var7 = (Entry) var6.next();
				String var8 = (String) var7.getKey();
				if ("name".equals(var8)) {
					((LWCTemplate) var2).setName((String) var7.getValue());
				} else {
					IPLMDictionaryPublicEntityItf var9 = getAttributeEntity(var1, var4, var8);
					if (var9 != null) {
						IPLMDictionaryPublicAttributeItf var10 = var9.getAttribute(var1, var8);
						String var11 = var10.getFullName(var1);
						if (var11.startsWith(".")) {
							var11 = var11.substring(1);
						}

						String var12 = this.normalizeAttribute(var1, (String) var7.getValue(), var10);
						((LWCTemplate) var2).setAttributeValue(var1, var11, var12);
					}
				}
			}

		}
	}

	protected String normalizeAttribute(Context var1, String var2, IPLMDictionaryPublicAttributeItf var3)
			throws MatrixException {
		String var4 = var2;
		String var5 = var3.getPrimitive(var1);
		byte var7 = -1;
		switch (var5.hashCode()) {
			case -672261858 :
				if (var5.equals("Integer")) {
					var7 = 0;
				}
				break;
			case 1729365000 :
				if (var5.equals("Boolean")) {
					var7 = 2;
				}
				break;
			case 2052876273 :
				if (var5.equals("Double")) {
					var7 = 1;
				}
		}

		switch (var7) {
			case 0 :
			case 1 :
			case 2 :
				if (var2.startsWith("\"") && var2.endsWith("\"")) {
					var4 = var2.substring(1, var2.length() - 1);
				}
			default :
				return var4;
		}
	}

	public void create(Context var1, String var2, String var3, Map<String, String> var4, Map<String, Object> var5,
			Map<String, String> var6, String var7) throws Exception {
		if (this.jsonTypes.contains(var3)) {
			this.jsonService.create(var2, var3, var4);
			this.toBeSavedJsonObjectIds.add(var2);
		} else if ("Path".equals(var3)) {
			this.createPath(var1, var2, var3, var4, var5);
		} else {
			var3 = var4 != null && var4.get("type") != null ? (String) var4.get("type") : var3;
			IPLMDictionaryPublicEntityItf var8 = getType(var1, var3);
			this.processCreateClass(var1, var2, var3, var4, var5, var6, var7);
			if (var4 != null && var4.get("Content Data") != null && var8.isKindOf(var1, "Requirement")) {
				IRichEditUtil var9 = RichEditFactory.getRichEditUtil();
				var9.saveContent(var1, var2, Format.HTML, (String) var4.get("Content Data"));
			}

		}
	}

	protected void updateInsideRep(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		CompositeId var9 = new CompositeId(var2);
		if (var9.isComposite() && this.jsonService.getModelById(var9.getObjectId()) == null) {
			String var10 = this.streamManager.readStream(var1, var9.getRepresentationId(), var9.getFileName());
			Set var11 = this.jsonService.deserialize(var10);
			Iterator var12 = var11.iterator();

			while (var12.hasNext()) {
				String var13 = (String) var12.next();
				this.repIdsByRootContainerId.put(var13, var9.getRepresentationId());
			}
		}

		this.jsonService.update(var9.getObjectId(), var3, var4, var5, var6);
		this.toBeSavedJsonObjectIds.add(var2);
	}

	protected void updatePathElements(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		TPath var9 = (TPath) this.pathsById.get(var2);
		if (var9 != null) {
			var9.setElements(var6, var8);
		}

	}

	protected void updatePaths(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		ArrayList var9 = new ArrayList(Arrays.asList(parsePath(var6)));
		ArrayList var10 = new ArrayList(Arrays.asList(parseExistingPath(var5)));
		var10.removeAll(Arrays.asList(parseExistingPath(var6)));
		var9.removeAll(Arrays.asList(parsePath(var5)));
		String var11 = var1.getCustomData("Change Id");
		Iterator var13 = var9.iterator();

		String var14;
		while (var13.hasNext()) {
			var14 = (String) var13.next();
			TPath var15 = (TPath) this.pathsById.get(var14);
			if (var15 != null) {
				String var12 = addPathToBusinessObject(var1, var15, var2, var7, var8);
				if (var11 != null) {
					LWCServices.beginUserfact(var1, "MODIFY", "");
					LWCEvent var16 = new LWCEvent(var2, false, "Update", "Add Path " + var12);
					LWCServices.logEvents(var1, Arrays.asList(var16));
					LWCServices.endUserfact(var1);
				}
			}
		}

		if (var11 != null) {
			var13 = var10.iterator();

			while (var13.hasNext()) {
				var14 = (String) var13.next();
				LWCServices.beginUserfact(var1, "MODIFY", "");
				LWCEvent var17 = new LWCEvent(var2, false, "Update", "Remove Path " + var14);
				LWCServices.logEvents(var1, Arrays.asList(var17));
				LWCServices.endUserfact(var1);
			}
		}

	}

	protected void updatePath(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		if ("AppIndex".equals(var4)) {
			Path var9 = new Path(var2);
			AttributeList var10 = new AttributeList();
			var10.add(new Attribute(new AttributeType(var4), var6));
			var9.setAttributes(var1, var10);
		}

	}

	protected void updatePort(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		Object var9 = var7.get(var2);
		BusinessObject var10 = null;
		if (var9 == null) {
			var10 = this.getBusinessObjectByPhysicalId(var1, var2);
		}

		if (var9 instanceof BusinessObject) {
			var10 = (BusinessObject) var9;
		}

		StringList var11 = new StringList();
		var11.add("from[" + var4 + "].to.id");
		var11.add("from[" + var4 + "].to.physicalid");
		BusinessObjectWithSelect var12 = var10.select(var1, var11);
		String var13 = var12.getSelectData("from[" + var4 + "].to.physicalid");
		String var14;
		if ("".equals(var13)) {
			var14 = UUID.randomUUID().toString();
			HashMap var15 = new HashMap();
			var15.put("containedBy", var2);
			this.processCreateClass(var1, var14, var4, var15, var7, var8, var2);
			var13 = (String) var8.get(var14);
		} else {
			var14 = var1.getCustomData("Change Id");
			if (var14 != null) {
				LWCServices.beginUserfact(var1, "MODIFY", "");
				LWCEvent var23 = new LWCEvent(var2, false, "Update", "Update Port " + var13);
				LWCServices.logEvents(var1, Arrays.asList(var23));
				LWCServices.endUserfact(var1);
			}
		}

		TPath var22 = (TPath) this.pathsById.get(var6);
		AttributeList var24 = new AttributeList();
		SemanticRelation[] var16 = SemanticRelation.values();
		SemanticRelation var17 = null;
		SemanticRelation[] var18 = var16;
		int var19 = var16.length;

		for (int var20 = 0; var20 < var19; ++var20) {
			SemanticRelation var21 = var18[var20];
			if (var21.name().startsWith(var4)) {
				var17 = var21;
				break;
			}
		}

		var24.add(new Attribute(new AttributeType("RoleSemantics"), var17.name()));
		var24.add(new Attribute(new AttributeType("Role"), String.valueOf(var17.role)));
		var24.add(new Attribute(new AttributeType("Semantics"), String.valueOf(var17.semantic)));
		if (var22 == null) {
			String var25 = UUID.randomUUID().toString();
			var22 = new TPath(var25, var4, "Reference3", var24);
			this.pathsById.put(var25, var22);
			var22.setElements(var6, var8);
		} else {
			var22.attributes = var24;
		}

		addPathToBusinessObject(var1, var22, var13, var7, var8);
	}

	protected void updateBusinessObjectOrRelationship(Context var1, String var2, String var3, String var4, String var5,
			String var6, Map<String, Object> var7, Map<String, String> var8) throws Exception {
		String var9 = var1.getCustomData("Change Id");
		Object var10 = var7.get(var2);
		BusinessObject var11 = null;
		Relationship var12 = null;
		if (var10 == null) {
			var11 = this.getBusinessObjectByPhysicalId(var1, var2);
			if (var11 == null) {
				var12 = new Relationship(var2);
				var12.open(var1);
				var10 = var12;
			}
		}

		HashMap var13 = new HashMap();
		if (var10 instanceof BusinessObject) {
			var11 = (BusinessObject) var10;
		}

		IPLMDictionaryPublicEntityItf var14;
		IPLMDictionaryPublicAttributeItf var15;
		String var16;
		if (var10 instanceof Relationship) {
			var14 = getAttributeEntity(var1, var12.getTypeName(), var4);
			if (var14 != null) {
				var15 = var14.getAttribute(var1, var4);
				if (var15 != null) {
					var16 = var15.getFullName(var1);
					var12 = (Relationship) var10;
					AttributeList var26 = new AttributeList();
					var13.put(var16, var6);
					var26.add(new Attribute(new AttributeType(var16), var6));
					var12.setAttributeValues(var1, var26);
					if (var9 != null) {
						LWCServices.beginUserfact(var1, "MODIFY", "");
						LWCEvent var28 = new LWCEvent(var2, true, "Update", "modify " + var16 + ": " + var6);
						LWCServices.logEvents(var1, Arrays.asList(var28));
						LWCServices.endUserfact(var1);
					}

				}
			}
		} else {
			try {
				if (var11.getTypeName() == null) {
					var11.open(var1);
				}

				var14 = getAttributeEntity(var1, var11.getTypeName(), var4);
				if (var14 != null) {
					var15 = var14.getAttribute(var1, var4);
					if (var15 != null) {
						var16 = var15.getFullName(var1);
						var13.put(var16, var6);
						if (var14.isIPLMDictionaryInterface()) {
							BusinessInterface var17 = new BusinessInterface(var14.getName(), var1.getVault());
							BusinessInterfaceList var18 = var11.getBusinessInterfaces(var1);
							boolean var19 = true;
							if (var18.contains(var17)) {
								var19 = false;
							}

							Iterator var20 = var18.iterator();

							while (var20.hasNext()) {
								Object var21 = var20.next();
								BusinessInterface var22 = (BusinessInterface) var21;
								if (var14.getName().equals(var22.getName())) {
									var19 = false;
									break;
								}
							}

							if (var19) {
								var11.addBusinessInterface(var1, var17);
							}
						}

						if (var15.isMultiValuated(var1)) {
							AttributeType var24 = new AttributeType(var16);
							AttributeList var27 = new AttributeList();
							String[] var29 = var6.split(",");
							StringList var30 = new StringList(var29);
							var27.add(new Attribute(var24, var30));
							var11.setAttributeValues(var1, var27);
						} else {
							var11.setAttributeValue(var1, var16, var6);
						}

						if (var9 != null) {
							LWCServices.beginUserfact(var1, "MODIFY", "");
							LWCEvent var25 = new LWCEvent(var2, false, "Update", "modify " + var16 + ": " + var6);
							LWCServices.logEvents(var1, Arrays.asList(var25));
							LWCServices.endUserfact(var1);
						}

					}
				}
			} catch (MatrixException var23) {
				Logger.error("Cannot set attribute " + var4 + " for object " + var2, var23);
				throw var23;
			}
		}
	}

	public void update(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		if (this.relationsToRep.containsKey(var3 + "." + var4)) {
			String var9 = (String) this.relationsToRep.get(var3 + "." + var4);
			this.addToRep(var1, var2, var3, var4, var9, var6, var7, var8);
		} else if (this.jsonTypes.contains(var3)) {
			this.updateInsideRep(var1, var2, var3, var4, var5, var6, var7, var8);
		} else if ("elements".equals(var4)) {
			this.updatePathElements(var1, var2, var3, var4, var5, var6, var7, var8);
		} else if ("paths".equals(var4)) {
			this.updatePaths(var1, var2, var3, var4, var5, var6, var7, var8);
		} else if ("Path".equals(var3)) {
			this.updatePath(var1, var2, var3, var4, var5, var6, var7, var8);
		} else if (this.isPort(var1, var4)) {
			this.updatePort(var1, var2, var3, var4, var5, var6, var7, var8);
		} else {
			this.updateBusinessObjectOrRelationship(var1, var2, var3, var4, var5, var6, var7, var8);
		}

	}

	protected boolean isPort(Context var1, String var2) throws MatrixException {
		IPLMDictionaryPublicClassItf var3 = dictionary.getClass(var1, var2);
		return var3 != null ? var3.isKindOf(var1, "PLMPort") : false;
	}

	protected static IPLMDictionaryPublicEntityItf getType(Context var0, String var1) throws MatrixException {
		Object var2 = dictionary.getClass(var0, var1);
		if (var2 == null) {
			var2 = dictionary.getRelationClass(var0, var1);
		}

		return (IPLMDictionaryPublicEntityItf) var2;
	}

	protected static IPLMDictionaryPublicEntityItf getAttributeEntity(Context var0, String var1, String var2)
			throws MatrixException {
		IPLMDictionaryPublicEntityItf var3 = getType(var0, var1);
		if (var3 != null) {
			IPLMDictionaryPublicAttributeItf var4 = var3.getAttribute(var0, var2);
			if (var4 != null && !var4.getFullName(var0).startsWith("BusinessType")) {
				return var3;
			}

			List var5 = var3.getExtendingInterfaces(var0, true, true, true);
			Iterator var6 = var5.iterator();

			while (var6.hasNext()) {
				IPLMDictionaryPublicInterfaceItf var7 = (IPLMDictionaryPublicInterfaceItf) var6.next();
				var4 = var7.getAttribute(var0, var2);
				if (var4 != null && !var4.getFullName(var0).startsWith("BusinessType")) {
					return var7;
				}
			}
		}

		return null;
	}

	protected void createPath(Context var1, String var2, String var3, Map<String, String> var4,
			Map<String, Object> var5) {
		AttributeList var6 = new AttributeList();
		String var7 = (String) var4.get("RoleSemantics");
		String var8 = (String) var4.get("AppIndex");
		String var9 = (String) var4.get("IDRel");
		SemanticRelation var10 = SemanticRelation.valueOf(var7);
		var6.add(new Attribute(new AttributeType("RoleSemantics"), var7));
		var6.add(new Attribute(new AttributeType("Role"), String.valueOf(var10.role)));
		var6.add(new Attribute(new AttributeType("Semantics"), String.valueOf(var10.semantic)));
		var6.add(new Attribute(new AttributeType("AppIndex"), var8 != null ? var8 : "0"));
		var6.add(new Attribute(new AttributeType("IDRel"), var9 != null ? var9 : "0"));
		int var11 = var7.lastIndexOf("Reference");
		String var12 = var7.substring(0, var11);
		String var13 = var7.substring(var11);
		TPath var14 = new TPath(var2, var12, var13, var6);
		this.pathsById.put(var2, var14);
	}

	protected static String[] parsePath(String var0) {
		if (var0 != null && var0.charAt(0) == '[' && var0.charAt(var0.length() - 1) == ']') {
			ArrayList var1 = new ArrayList();
			Matcher var2 = WordPattern.matcher(var0);

			while (var2.find()) {
				var1.add(var2.group(1));
			}

			return (String[]) var1.toArray(new String[0]);
		} else {
			return new String[]{var0};
		}
	}

	protected static String[] parseExistingPath(String var0) {
		if (var0 != null && var0.charAt(0) == '[' && var0.charAt(var0.length() - 1) == ']') {
			ArrayList var1 = new ArrayList();
			Pattern var2 = Pattern.compile("\"\\w+\\.[\\w\\.]+\\w\"");
			Matcher var3 = var2.matcher(var0);

			while (var3.find()) {
				var1.add(var3.group());
			}

			return (String[]) var1.toArray(new String[0]);
		} else {
			return new String[]{var0};
		}
	}

	protected static String addPathToBusinessObject(Context var0, TPath var1, String var2, Map<String, Object> var3,
			Map<String, String> var4) throws MatrixException {
		BusinessObject var5 = (BusinessObject) var3.get(var2);
		if (var5 == null) {
			StringList var6 = new StringList();
			var6.add("physicalid");
			String[] var7 = new String[]{var2};
			BusinessObjectWithSelectList var8 = BusinessObject.getSelectBusinessObjectData(var0, var7, var6);
			if (var8.size() == 1) {
				var5 = (BusinessObject) var8.getElement(0);
				var3.put(var2, var5);
			}
		}

		var5.open(var0);
		String var9 = var5.addPath(var0, "SemanticRelation", var1.getPathElements(var0), var1.getAttributes());
		Logger.debug("Added path {} to {}", var9, var2);
		var4.put(var1.id, var9);
		return var9;
	}

	protected BusinessObject getBusinessObjectByPhysicalId(Context var1, String var2) throws Exception {
		String var3 = MqlUtil.mqlCommand(var1, "temp query bus $1 $2 $3 where $4 select $5 dump",
				new String[]{"*", "*", "*", "physicalid=='" + var2 + "'", "id"});
		int var4 = var3.lastIndexOf(44) + 1;
		String var5 = var3.substring(var4);
		return var5.length() > 0 ? new BusinessObject(var5) : null;
	}

	protected void addToRep(Context var1, String var2, String var3, String var4, String var5, String var6,
			Map<String, Object> var7, Map<String, String> var8) throws Exception {
		Logger.debug("Saving to JSON stream {} {} {} {} {}", new Object[]{var2, var3, var4, var5, var6});
		Model var10 = this.jsonService.getModelById(var6);
		if (var10 != null && var10.getType() != null) {
			String var9 = var10.getType();
			HashSet var11 = new HashSet();
			String var12 = this.jsonService.serialize(var6, var11);
			BusinessObject var13 = (BusinessObject) var7.get(var2);
			if (var13 == null) {
				var13 = this.getBusinessObjectByPhysicalId(var1, var2);
				var7.put(var2, var13);
			}

			IPLMDictionaryPublicRelationClassItf var14 = dictionary.getRelationClass(var1, var5);
			String var15 = (String) var14.getReferencedClassNames(var1, 1).get(0);
			StringList var16 = new StringList();
			var16.add("from[" + var5 + "].to.id");
			var16.add("from[" + var5 + "].to.physicalid");
			var16.add("from[" + var5 + "].attribute[" + "PLMInstance.PLM_ExternalID" + "]");
			BusinessObjectWithSelect var17 = var13.select(var1, var16);
			String var18 = "";
			String var19 = "";
			StringList var20 = var17.getSelectDataList("from[" + var5 + "].to.physicalid");
			StringList var21 = var17
					.getSelectDataList("from[" + var5 + "].attribute[" + "PLMInstance.PLM_ExternalID" + "]");
			if (var20 != null) {
				for (int var22 = 0; var22 < var20.size(); ++var22) {
					if (var20.get(var22) != null && !((String) var20.get(var22)).isEmpty() && var21.get(var22) != null
							&& ((String) var21.get(var22)).equals(var4)) {
						var18 = (String) var20.get(var22);
						var19 = (String) var21.get(var22);
						break;
					}
				}
			}

			if (var18 == null || var18.isEmpty() || var19 == null || !var19.equals(var4)) {
				String var23 = "VPLM_SMB_Definition";
				var18 = this.createRep(var1, var15, var23, var7, var8);
				this.createRepInstance(var1, var18, var5, var4, var7, var8, var2);
			}

			this.streamManager.saveStream(var1, var18, var15, var12, var9);
			this.repIdsByRootContainerId.put(var6, var18);
			this.savedRepresentationPhysicalIds.add(var18);
			Map var24 = this.enrichJSONIds(var11, var18, var9);
			var8.putAll(var24);
		} else {
			Logger.error("No type for model with id: " + var6);
			throw new Exception("No type for model with id: " + var6);
		}
	}

	protected String createRep(Context var1, String var2, String var3, Map<String, Object> var4,
			Map<String, String> var5) throws MatrixException {
		String var6 = UUID.randomUUID().toString();
		HashMap var7 = new HashMap();
		if (var3 != null) {
			var7.put("policy", var3);
		}

		this.processCreateClass(var1, var6, var2, var7, var4, var5, (String) null);
		String var8 = (String) var5.get(var6);
		return var8;
	}

	protected String createRepInstance(Context var1, String var2, String var3, String var4, Map<String, Object> var5,
			Map<String, String> var6, String var7) throws MatrixException {
		HashMap var8 = new HashMap();
		var8.put("PLMInstance.PLM_ExternalID", var4);
		var8.put("containedBy", var7);
		var8.put("instanceOf", var2);
		String var9 = UUID.randomUUID().toString();
		this.processCreateClass(var1, var9, var3, var8, var5, var6, var7);
		String var10 = (String) var6.get(var9);
		return var10;
	}

	public void postUpdate(Context var1, List<Throwable> var2, List<Throwable> var3, Map<String, Object> var4,
			Map<String, String> var5) throws Exception {
		HashSet var6 = new HashSet();
		Iterator var7 = this.toBeSavedJsonObjectIds.iterator();

		String var10;
		while (var7.hasNext()) {
			String var8 = (String) var7.next();
			CompositeId var9 = new CompositeId(var8);
			var10 = var9.getObjectId();
			var6.add(var10);
		}

		Set var17 = this.jsonService.getSaveables(var6);
		Iterator var18 = var17.iterator();

		while (var18.hasNext()) {
			String var19 = (String) var18.next();
			var10 = (String) this.repIdsByRootContainerId.get(var19);
			String var11 = this.findRepresentationType(var1, var10, var4);
			Model var12 = this.jsonService.getModelById(var19);
			String var13 = var12.getType();
			HashSet var14 = new HashSet();
			String var15 = this.jsonService.serialize(var19, var14);
			Map var16 = this.enrichJSONIds(var14, var10, var13);
			var5.putAll(var16);
			this.streamManager.saveStream(var1, var10, var11, var15, var13);
		}

		this.toBeSavedJsonObjectIds.clear();
		this.savedRepresentationPhysicalIds.clear();
	}

	protected Map<String, String> enrichJSONIds(Set<String> var1, String var2, String var3) {
		HashMap var4 = new HashMap();
		Iterator var5 = var1.iterator();

		while (var5.hasNext()) {
			String var6 = (String) var5.next();
			StringBuilder var7 = new StringBuilder(var2);
			var7.append("_''.1=");
			var7.append(var3);
			String var8 = var7.toString();
			CompositeId var9 = new CompositeId(var2, var8, var6);
			var4.put(var6, var9.toString());
		}

		return var4;
	}

	protected String findRepresentationType(Context var1, String var2, Map<String, Object> var3) throws Exception {
		String var4 = null;
		Object var5 = var3 != null ? var3.get(var2) : null;
		if (var5 instanceof BusinessObject) {
			var4 = ((BusinessObject) var5).getTypeName();
		} else {
			BusinessObject var6 = this.getBusinessObjectByPhysicalId(var1, var2);
			if (var6 != null) {
				if (!var6.isOpen()) {
					var6.open(var1);
				}

				var4 = var6.getTypeName();
			}
		}

		return var4;
	}

	public void preprocessUpdates(List<UpdateStatement> var1) {
      var1.sort(new PLMPersistenceService1());
   }

	public void preprocessCreates(List<CreateStatement> var1) {
      var1.sort(new PLMPersistenceService2());
   }

	static {
		dictionary = factory.getDictionary();
	}
	
	class PLMPersistenceService1 implements Comparator<UpdateStatement> {
		public int compare(UpdateStatement var1, UpdateStatement var2) {
			int var3 = 0;
			boolean var4 = "paths".equals(var1.getAttributeName())
					|| "FailureModesEffectsAnalysisAnalysisContext".equals(var1.getAttributeName());
			boolean var5 = "paths".equals(var2.getAttributeName())
					|| "FailureModesEffectsAnalysisAnalysisContext".equals(var2.getAttributeName());
			if (!var4 && !var5) {
				if (relationsToRep.containsKey(var1.getAttributeName())) {
					++var3;
				}

				if (relationsToRep.containsKey(var2.getAttributeName())) {
					--var3;
				}

				return var3;
			} else {
				if (var4) {
					++var3;
				}

				if (var5) {
					--var3;
				}

				return var3;
			}
		}
	}
	
	class PLMPersistenceService2 implements Comparator<CreateStatement> {
		public int compare(CreateStatement var1, CreateStatement var2) {
			int var3 = 0;
			String var4 = (String) var1.getAttributes().get("instanceOf");
			String var5 = (String) var1.getAttributes().get("containedBy");
			String var6 = (String) var2.getAttributes().get("instanceOf");
			String var7 = (String) var2.getAttributes().get("containedBy");
			String var8 = var1.getId();
			String var9 = var2.getId();
			if (var4 != null && (var9.equals(var4) || var9.equals(var5))) {
				++var3;
			} else if (var6 != null && (var8.equals(var6) || var8.equals(var7))) {
				--var3;
			}

			return var3;
		}
	}
	
	public class CompositeId {
		protected static final String separator = "/";
		protected String representationId;
		protected String fileName;
		protected String objectId;

		public CompositeId(String var2, String var3, String var4) {
			this.representationId = var2;
			this.fileName = var3;
			this.objectId = var4;
		}

		public CompositeId(String var2) {
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
	
	public enum SemanticRelation {
		FallibleReferenceStandsForReference3(410, 5), FailureModesEffectsAnalysisCauseFailureModeReference3(422,
				5), FailureModesEffectsAnalysisCauseOccurrenceReference3(423,
						5), FailureModesEffectsAnalysisEffectsWithoutBarrierReference3(424,
								5), FailureModesEffectsAnalysisEffectsWithBarrierReference3(425,
										5), FailureModesEffectsAnalysisAnalyzesReference3(426,
												5), FailureModesEffectsAnalysisAnalysisContextReference3(427,
														5), FailureModesEffectsAnalysisEffectsWithoutBarrierOccurrenceReference3(
																441,
																5), FailureModesEffectsAnalysisEffectsWithBarrierOccurrenceReference3(
																		442,
																		5), FailureModesEffectsAnalysisDetectionReference3(
																				443,
																				5), FailureModesEffectsAnalysisPreventionReference3(
																						444,
																						5), PLM_ImplementLink_TargetReference3(
																								51,
																								5), PLM_ImplementLink_SourceReference5(
																										52,
																										7), Is_referencing_OperationReference3(
																												453,
																												5), Is_referencing_ProductCharacteristicReference(
																														451,
																														1), Is_referencing_ProcessCharacteristicReference(
																																452,
																																1);

		protected int role;
		protected int semantic;

		private SemanticRelation(int var3, int var4) {
			this.role = var3;
			this.semantic = var4;
		}
	}
	
	public class TPath {
		private static final String APP_INDEX = "AppIndex";
		private static final String ID_REL = "IDRel";
		private static final String OUT_OF_SCOPES = "OutOfScopes";
		private static final String RELEVANT = "relevant";
		private static final String LOGICALID = "logicalid";
		private static final String MAJORID = "majorid";
		private static final String UPDATESTAMP = "updatestamp";
		private static final String PHYSICALID = "physicalid";
		private static final String TYPE = "type";
		protected String id;
		protected AttributeList attributes;
		protected String[] elements;
		protected String role;
		protected String semantic;

		public TPath(String var2, String var3, String var4,
				AttributeList var5) {
			this.elements = null;
			this.role = null;
			this.semantic = null;
			this.id = var2;
			this.attributes = var5;
			this.role = var3;
			this.semantic = var4;
		}

		public void setElements(String var1, Map<String, String> var2) {
			if (var1 != null && var1.charAt(0) == '[' && var1.charAt(var1.length() - 1) == ']') {
				ArrayList var6 = new ArrayList();
				Matcher var4 = WordPattern.matcher(var1);

				while (var4.find()) {
					String var5 = (String) var2.get(var4.group(1));
					var6.add(var5 != null ? var5 : var4.group(1));
				}

				this.elements = (String[]) var6.toArray(new String[0]);
			} else {
				String var3 = (String) var2.get(var1);
				this.elements = new String[]{var3 != null ? var3 : var1};
			}
		}

		public Element[] getPathElements(Context var1) throws MatrixException {
			StringList var2 = new StringList();
			var2.add("physicalid");
			var2.add("updatestamp");
			var2.add("majorid");
			var2.add("logicalid");
			var2.add("type");
			var2.add("relevant");
			Element[] var3 = null;
			RelationshipWithSelectList var4 = Relationship.getSelectRelationshipData(var1, this.elements, var2);
			BusinessObjectWithSelectList var5 = BusinessObject.getSelectBusinessObjectData(var1, this.elements, var2);
			var3 = new Element[this.elements.length];

			for (int var6 = 0; var6 < this.elements.length; ++var6) {
				RelationshipWithSelect var7 = (RelationshipWithSelect) var4.getElement(var6);
				BusinessObjectWithSelect var8 = (BusinessObjectWithSelect) var5.getElement(var6);
				boolean var9 = var7.getSelectData("physicalid") != null && !var7.getSelectData("physicalid").isEmpty();
				if (var9) {
					var3[var6] = new Element(var6 + 1, 2, var7.getSelectData("type"), var7.getSelectData("physicalid"),
							var7.getSelectData("majorid"), var7.getSelectData("logicalid"),
							var7.getSelectData("updatestamp"), true);
				} else {
					var3[var6] = new Element(var6 + 1, 1, var8.getSelectData("type"), var8.getSelectData("physicalid"),
							var8.getSelectData("majorid"), var8.getSelectData("logicalid"),
							var8.getSelectData("updatestamp"), true);
				}
			}

			return var3;
		}

		public AttributeList getAttributes() {
			String var1 = "0";

			for (Integer var2 = 1; var2 < this.elements.length; var2 = var2 + 1) {
				var1 = var1 + "|0";
			}

			this.attributes.add(new Attribute(new AttributeType("OutOfScopes"), var1));
			return this.attributes;
		}

		public String getId() {
			return this.id;
		}

		public String getRole() {
			return this.role;
		}

		public String getSemantic() {
			return this.semantic;
		}
	}
}