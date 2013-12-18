package org.GreenTeaScript;

import java.util.ArrayList;
import java.util.HashMap;

class KonohaByteCodeWriter {
	///*field*/private ArrayList<String> HeaderCode;
	/*field*/private ArrayList<Object> ConstCode;
	/*field*/private ArrayList<ArrayList<Object>> ClassCodeList;
	/*field*/private ArrayList<Object> VisittingClassField;
	/*field*/private ArrayList<ArrayList<Object>> MethodCodeList;
	/*field*/private ArrayList<Object> VisittingMethodParam;
	/*field*/private ArrayList<Object> VisittingMethodBody;
	/*field*/private HashMap<Integer, Integer> LabelMap;
	/*field*/private int MainFuncIndex;

	public static final int OPCODE_NOP = 0;
	/*public static final int OPCODE_THCODE = 1;
	public static final int OPCODE_ENTER = 2;
	public static final int OPCODE_EXIT = 3;
	public static final int OPCODE_NSET = 4;
	public static final int OPCODE_NMOV = 5;
	public static final int OPCODE_NMOVx = 6;
	public static final int OPCODE_XNMOV = 7;
	public static final int OPCODE_NEW = 8;
	public static final int OPCODE_NUL = 9;
	public static final int OPCODE_BOX = 10;
	public static final int OPCODE_BBOX = 11;
	public static final int OPCODE_LOOKUP = 12;
	public static final int OPCODE_CALL = 13;
	public static final int OPCODE_RET = 14;
	public static final int OPCODE_NCALL = 15;*/
	public static final int OPCODE_JMP = 16;
	public static final int OPCODE_JMPF = 17;
	/*public static final int OPCODE_TRYJMP = 18;
	public static final int OPCODE_YIELD = 19;
	public static final int OPCODE_ERROR = 20;
	public static final int OPCODE_SAFEPOINT = 21;
	public static final int OPCODE_CHKSTACK = 22;*/


	public KonohaByteCodeWriter/*constructor*/() {
		//this.HeaderCode = new ArrayList<String>();
		this.ConstCode = new ArrayList<Object>();
		this.ClassCodeList = new ArrayList<ArrayList<Object>>();
		this.MethodCodeList = new ArrayList<ArrayList<Object>>();
		this.VisittingMethodParam = new ArrayList<Object>();
		this.LabelMap = new HashMap<Integer, Integer>();
		this.MainFuncIndex = -1;
	}

	public void WriteNewConst(int TypeNumber, Object Constant) {
		if(Constant instanceof String) {
			/*local*/String ConstString = Constant.toString();
			this.ConstCode.add(new Integer(TypeNumber));
			this.ConstCode.add(new Integer(ConstString.length()));
			this.ConstCode.add(ConstString);
		}
		else {
			this.ConstCode.add(new Integer(TypeNumber));
			if(Constant instanceof Integer) { //boolean value
				this.ConstCode.add(new Integer(2));
			}
			else { //Long(int value) or Double(float value)
				this.ConstCode.add(new Integer(4));
			}
			this.ConstCode.add(Constant);
		}
	}

	public void CreateNewClassField() {
		this.VisittingClassField = new ArrayList<Object>();
	}
	public void WriteClassField(int TypeSource, int TypeNum, String FieldName) {
		this.VisittingClassField.add(new Integer(TypeSource));
		this.VisittingClassField.add(new Integer(TypeNum));
		this.VisittingClassField.add(new Integer(FieldName.length()));
		this.VisittingClassField.add(FieldName);
	}
	public void SetNewClass(String ClassName) {
		/*local*/int ClassFieldSize = LibGreenTea.ListSize(this.VisittingClassField) / 4;
		this.VisittingClassField.add(0, new Integer(ClassName.length()));
		this.VisittingClassField.add(1, ClassName);
		this.VisittingClassField.add(2, new Integer(ClassFieldSize));

		this.ClassCodeList.add(this.VisittingClassField);
		this.VisittingClassField = null;
	}

	public void CreateNewMethodBody() {
		this.VisittingMethodParam.clear();
		this.LabelMap.clear();
		this.VisittingMethodBody = new ArrayList<Object>();
	}
	public void WriteMethodParam(int TypeSource, int TypeNum) {
		this.VisittingMethodParam.add(new Integer(TypeSource));
		this.VisittingMethodParam.add(new Integer(TypeNum));
	}
	public void WriteOpCode(int OpCode, int Operand1, int Operand2, int Operand3) {
		this.VisittingMethodBody.add(new Integer(OpCode));
		this.VisittingMethodBody.add(new Integer(Operand1));
		this.VisittingMethodBody.add(new Integer(Operand2));
		this.VisittingMethodBody.add(new Integer(Operand3));
	}
	public void WriteNewLabel(int LabelNum) {
		this.LabelMap.put(LabelNum, this.VisittingMethodBody.size() / 4);
	}
	public void ResolveLabelOffset() {
		/*local*/int CodeSize = this.VisittingMethodBody.size() / 4;
		for(/*local*/int i = 0; i < CodeSize; ++i) {
			/*local*/int OpCode = ((/*cast*/Integer)this.VisittingMethodBody.get(4*i+0)).intValue();
			if(OpCode == OPCODE_JMP || OpCode == OPCODE_JMPF) {
				/*local*/Integer LabelNum = (/*cast*/Integer)this.VisittingMethodBody.get(4*i+1);
				/*local*/Integer LabelOffset = this.LabelMap.get(LabelNum);
				if(LabelOffset.intValue() < CodeSize) {
					this.VisittingMethodBody.set(4*i+1, LabelOffset);
				}
				else {
					this.VisittingMethodBody.set(4*i+0, new Integer(OPCODE_NOP));
					this.VisittingMethodBody.set(4*i+1, new Integer(0));
				}
			}
		}
	}
	public void SetNewMethod(int ReturnTypeSource, int ReturnTypeNum, String MethodName) {
		this.ResolveLabelOffset();
		/*local*/int MethodCodeSize = LibGreenTea.ListSize(this.VisittingMethodBody) / 4;
		/*local*/int HeaderPointer = 0;
		this.VisittingMethodBody.add(HeaderPointer++, new Integer(ReturnTypeSource));
		this.VisittingMethodBody.add(HeaderPointer++, new Integer(ReturnTypeNum));
		this.VisittingMethodBody.add(HeaderPointer++, new Integer(MethodName.length()));
		this.VisittingMethodBody.add(HeaderPointer++, MethodName);
		/*local*/int ParamSize = LibGreenTea.ListSize(this.VisittingMethodParam) / 2;
		this.VisittingMethodBody.add(HeaderPointer++, new Integer(ParamSize));
		for(/*local*/int i = 0; i < ParamSize; ++i) {
			this.VisittingMethodBody.add(HeaderPointer++, this.VisittingMethodParam.get(2*i+0));
			this.VisittingMethodBody.add(HeaderPointer++, this.VisittingMethodParam.get(2*i+1));
		}

		this.VisittingMethodBody.add(HeaderPointer++, new Integer(MethodCodeSize));

		this.MethodCodeList.add(this.VisittingMethodBody);
		if(MethodName.equals("main")) {
			this.MainFuncIndex = this.MethodCodeList.size() - 1;
		}
		this.VisittingMethodBody = null;
	}
	public void OutputByteCode(KonohaByteCodeGenerator Generator) {
		/*local*/String ByteCode = "";
		/*local*/int ConstSize = LibGreenTea.ListSize(this.ConstCode) / 3;
		ByteCode += this.ToByteCode(ConstSize);
		/*local*/int ClassSize = LibGreenTea.ListSize(this.ClassCodeList);
		ByteCode += this.ToByteCode(ClassSize);
		/*local*/int MethodSize = LibGreenTea.ListSize(this.MethodCodeList);
		ByteCode += this.ToByteCode(MethodSize);
		for(/*local*/int i = 0; i < ConstSize; ++i) {
			ByteCode += this.ToByteCode(this.ConstCode.get(3*i+0));
			ByteCode += this.ToByteCode(this.ConstCode.get(3*i+1));
			ByteCode += this.ToByteCode(this.ConstCode.get(3*i+2));
		}
		for(/*local*/int i = 0; i < ClassSize; ++i) {
			/*local*/ArrayList<Object> ClassCode = this.ClassCodeList.get(i);
			for(/*local*/int j = 0; j < ClassCode.size(); ++j) {
				ByteCode += this.ToByteCode(ClassCode.get(j));
			}
		}
		for(/*local*/int i = 0; i < MethodSize; ++i) {
			/*local*/ArrayList<Object> MethodCode = this.MethodCodeList.get(i);
			for(/*local*/int j = 0; j < MethodCode.size(); ++j) {
				ByteCode += this.ToByteCode(MethodCode.get(j));
			}
		}
		ByteCode += this.ToByteCode(this.MainFuncIndex);
		Generator.HeaderBuilder.Append(ByteCode);
	}
	private String ToByteCode(Object Value) {
		/*FIXME*/
		return Value.toString() + "\n";
	}
}

public class KonohaByteCodeGenerator extends GtSourceGenerator {
	/*field*/private ArrayList<Object> ConstPool;
	///*field*/private HashMap<Object, GtType> ConstTypeMap;
	/*field*/private ArrayList<GtFunc> MethodPool;
	/*field*/private ArrayList<GtType> ClassPool;
	/*field*/private HashMap<GtType, ArrayList<GtFieldInfo>> ClassFieldMap;
	/*field*/private int RegisterNum;
	/*field*/private ArrayList<Integer> RegStack;
	/*field*/private int LabelNum;
	/*field*/private ArrayList<Integer> ContinueStack;
	/*field*/private ArrayList<Integer> BreakStack;
	/*field*/private HashMap<String, Integer> LocalVarMap;
	/*field*/private KonohaByteCodeWriter Writer;

	private static final int CallParameters = 5;
	private static final int ThisIndex = 0;
	private static final int MethodIndex = -2;
	private static final int ReturnIndex = -8;

	private static final int OPCODE_NOP = 0;
	private static final int OPCODE_THCODE = 1;
	private static final int OPCODE_ENTER = 2;
	private static final int OPCODE_EXIT = 3;
	private static final int OPCODE_NSET = 4;
	private static final int OPCODE_NMOV = 5;
	private static final int OPCODE_NMOVx = 6;
	private static final int OPCODE_XNMOV = 7;
	private static final int OPCODE_NEW = 8;
	private static final int OPCODE_NUL = 9;
	private static final int OPCODE_BOX = 10;
	private static final int OPCODE_BBOX = 11;
	private static final int OPCODE_LOOKUP = 12;
	private static final int OPCODE_CALL = 13;
	private static final int OPCODE_RET = 14;
	private static final int OPCODE_NCALL = 15;
	private static final int OPCODE_JMP = 16;
	private static final int OPCODE_JMPF = 17;
	private static final int OPCODE_TRYJMP = 18;
	private static final int OPCODE_YIELD = 19;
	private static final int OPCODE_ERROR = 20;
	private static final int OPCODE_SAFEPOINT = 21;
	private static final int OPCODE_CHKSTACK = 22;

	private static final byte GT_TRUE = 1;
	private static final byte GT_FALSE = 0;

	private static final int NSET_InternalMethod = 0;
	private static final int NSET_UserConstant = 1;
	private static final int NSET_UserMethod = 2;
	
	private static final int TypeSource_Default = 0;
	private static final int TypeSource_UserDefined = 1;

	private static final int Op_Undefined = 0;
	private static final int Int_opMINUS = 1;
	private static final int Int_opADD = 2;
	private static final int Int_opSUB = 3;
	private static final int Int_opMUL = 4;
	private static final int Int_opDIV = 5;
	private static final int Int_opMOD = 6;
	private static final int Int_opEQ = 7;
	private static final int Int_opNEQ = 8;
	private static final int Int_opLT = 9;
	private static final int Int_opLTE = 10;
	private static final int Int_opGT = 11;
	private static final int Int_opGTE = 12;
	private static final int Array_get = 101;
	private static final int Array_set = 102;
	private static final int Array_newList = 103;
	

	public KonohaByteCodeGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
		super(TargetCode, OutputFile, GeneratorFlag);
		this.ConstPool = new ArrayList<Object>();
		//this.ConstTypeMap = new HashMap<Object, GtType>();
		this.MethodPool = new ArrayList<GtFunc>();
		this.ClassPool = new ArrayList<GtType>();
		this.ClassFieldMap = new HashMap<GtType, ArrayList<GtFieldInfo>>();
		this.RegisterNum = 0;
		this.RegStack = new ArrayList<Integer>();
		this.LabelNum = 0;
		this.ContinueStack = new ArrayList<Integer>();
		this.BreakStack = new ArrayList<Integer>();
		this.LocalVarMap = new HashMap<String, Integer>();
		this.Writer = new KonohaByteCodeWriter();
	}

	private boolean IsUnboxType(GtType Type) {
		if(Type.IsIntType()) {
			return true;
		}
		else if(Type.IsFloatType()) {
			return true;
		}
		else if(Type.IsBooleanType()) {
			return true;
		}
		return false;
	}
	private int GetDefaultTypeNumber(GtType Type) {
		if(Type.IsVoidType()) {
			return 0;
		}
		else if(Type.IsIntType()) {
			return 1;
		}
		else if(Type.IsFloatType()) {
			return 2;
		}
		else if(Type.IsBooleanType()) {
			return 3;
		}
		else if(Type.IsStringType()) {
			return 4;
		}
		/*else if(Type.IsArrayType()){
			return;
		}*/
		return -1;
	}
	@Override public void FlushBuffer() {
		this.Writer.OutputByteCode(this);
		super.FlushBuffer();
	}

	private void PushStack(ArrayList<Integer> Stack, int Number) {
		Stack.add(new Integer(Number));
	}
	private int PopStack(ArrayList<Integer> Stack) {
		/*local*/int Size = Stack.size();
		/*local*/Integer PopValue = Stack.remove(Size - 1);
		return PopValue.intValue();
	}
	private int PeekStack(ArrayList<Integer> Stack) {
		/*local*/int Size = Stack.size();
		return Stack.get(Size - 1);
	}
	private int AllocRegister() {
		/*FIXME*/
		return this.ReserveRegister(1);
	}
	private int ReserveRegister(int Size) {
		/*FIXME*/
		/*local*/int HeadRegister = this.RegisterNum;
		this.RegisterNum += Size * 2;
		return HeadRegister;
	}
	private void FreeRegister(int TargetReg) {
		/*FIXME*/
		this.RegisterNum = TargetReg;
	}
	private void PushRegister(int RegNum) {
		this.PushStack(this.RegStack, RegNum);
	}
	private int PopRegister() {
		return this.PopStack(this.RegStack);
	}
	private int ToUnboxReg(int RegNum) {
		if((RegNum % 2) == 1) { //Already Unbox
			return RegNum;
		}
		else {
			return RegNum + 1;
		}
	}
	private int ToBoxReg(int RegNum) {
		if((RegNum % 2) == 0) { //Already Box
			return RegNum;
		}
		else {
			return RegNum - 1;
		}
	}


	private int NewLabel() {
		return this.LabelNum++;
	}
	private void PushLoopLabel(int ContinueLabel, int BreakLabel) {
		this.PushStack(this.ContinueStack, ContinueLabel);
		this.PushStack(this.BreakStack, BreakLabel);
	}
	private void PopLoopLabel() {
		this.PopStack(this.ContinueStack);
		this.PopStack(this.BreakStack);
	}
	private int PeekContinueLabel() {
		return this.PeekStack(this.ContinueStack);
	}
	private int PeekBreakLabel() {
		return this.PeekStack(this.BreakStack);
	}
	private int AddConstant(Object ConstValue, GtType Type) {
		/*local*/int Index = this.ConstPool.indexOf(ConstValue);
		if(Index == -1) {
			Index = this.ConstPool.size();
			this.ConstPool.add(ConstValue);
			this.Writer.WriteNewConst(this.GetDefaultTypeNumber(Type), ConstValue);
		}
		return Index;
	}
	private int AddMethod(GtFunc DefinedFunc) {
		/*local*/int Index = this.MethodPool.indexOf(DefinedFunc);
		if(Index == -1) {
			Index = this.MethodPool.size();
			this.MethodPool.add(DefinedFunc);
		}
		return Index;
	}
	private int GetFieldOffset(ArrayList<GtFieldInfo> FieldList, String FieldName) {
		/*local*/int FieldSize = FieldList.size();
		/*local*/int Offset = -1;
		for(/*local*/int i = 0; i < FieldSize; ++i) {
			if(FieldList.get(i).NativeName.equals(FieldName)) {
				Offset = i;
				break;
			}
		}
		return Offset;
	}

	private void WriteOPNMOV(int TargetReg, int SourceReg) {
		/*local*/int Destination = TargetReg;
		if((SourceReg % 2) != (Destination % 2)) {
			if((SourceReg % 2) == 1) { //Unbox Value
				Destination += 1;
			}
			else if((SourceReg % 2) == 0) { //Box Value
				Destination -= 1;
			}
		}
		this.Writer.WriteOpCode(OPCODE_NMOV, Destination, SourceReg, 0);
	}
	private int GetMethodByUnary(String Op, GtType Type) {
		/*FIXME*/
		return Op_Undefined;
	}
	private int GetMethodByBinary(String Op, GtType Type) {
		/*FIXME*/
		int Method = Op_Undefined;
		if(Op.equals("+")) {
			if(Type.IsIntType()) {
				Method = Int_opADD;
			}
		}
		else if(Op.equals("-")) {
			if(Type.IsIntType()) {
				Method = Int_opSUB;
			}
		}
		else if(Op.equals("*")) {
			if(Type.IsIntType()) {
				Method = Int_opMUL;
			}
		}
		else if(Op.equals("/")) {
			if(Type.IsIntType()) {
				Method = Int_opDIV;
			}
		}
		else if(Op.equals("%")) {
			if(Type.IsIntType()) {
				Method = Int_opMOD;
			}
		}
		else if(Op.equals("==")) {
			if(Type.IsIntType()) {
				Method = Int_opEQ;
			}
		}
		else if(Op.equals("!=")) {
			if(Type.IsIntType()) {
				Method = Int_opNEQ;
			}
		}
		else if(Op.equals("<")) {
			if(Type.IsIntType()) {
				Method = Int_opLT;
			}
		}
		else if(Op.equals("<=")) {
			if(Type.IsIntType()) {
				Method = Int_opLTE;
			}
		}
		else if(Op.equals(">")) {
			if(Type.IsIntType()) {
				Method = Int_opGT;
			}
		}
		else if(Op.equals(">=")) {
			if(Type.IsIntType()) {
				Method = Int_opGTE;
			}
		}
		return Method;
	}

	private void WriteInternalMethodCall(int CallReg, int ParamSize, int MethodNum) {
		/*FIXME*/
		/*local*/boolean IsVoidMethod = false;
		/*local*/boolean DoesReturnUnboxValue = false;
		switch(MethodNum) {
		case Int_opMINUS:
		case Int_opADD:
		case Int_opSUB:
		case Int_opMUL:
		case Int_opDIV:
		case Int_opMOD:
		case Int_opEQ:
		case Int_opNEQ:
		case Int_opLT:
		case Int_opLTE:
		case Int_opGT:
		case Int_opGTE:
			IsVoidMethod = false;
			DoesReturnUnboxValue = true;
			break;
		case Array_get:
			IsVoidMethod = false;
			//ReturnUnboxValue = ;
			break;
		case Array_set:
			IsVoidMethod = true;
			break;
		case Array_newList:
			IsVoidMethod = false;
			DoesReturnUnboxValue = false;
			break;
		}

		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+MethodIndex), NSET_InternalMethod, MethodNum);
		this.Writer.WriteOpCode(OPCODE_CALL, CallReg, CallReg+ParamSize*2+2, 0);
		if(IsVoidMethod) {
			this.FreeRegister(this.ToBoxReg(CallReg - ThisIndex + ReturnIndex));
		}
		else {
			/*local*/int ReturnReg = CallReg - ThisIndex + ReturnIndex;
			if(DoesReturnUnboxValue) {
				ReturnReg = this.ToUnboxReg(ReturnReg);
			}
			this.PushRegister(ReturnReg);
			this.FreeRegister(this.ToBoxReg(ReturnReg + 2));
		}
	}

	@Override public void VisitEmptyNode(GtEmptyNode Node) {
		/*FIXME*/
	}

	@Override public void VisitNullNode(GtNullNode Node) {
		/*local*/int Reg = this.AllocRegister();
		this.Writer.WriteOpCode(OPCODE_NUL, Reg, 0, 0);
		this.PushRegister(Reg);
	}

	@Override public void VisitBooleanNode(GtBooleanNode Node) {
		/*local*/int Index;
		if(Node.Value) {
			Index = this.AddConstant(new Integer(GT_TRUE), Node.Type);
		}
		else {
			Index = this.AddConstant(new Integer(GT_FALSE), Node.Type);
		}
		/*local*/int Reg = this.ToUnboxReg(this.AllocRegister());
		this.Writer.WriteOpCode(OPCODE_NSET, Reg, NSET_UserConstant, Index);
		this.PushRegister(Reg);
	}

	@Override public void VisitIntNode(GtIntNode Node) {
		/*local*/int Index = this.AddConstant(new Long(Node.Value), Node.Type);
		/*local*/int Reg = this.ToUnboxReg(this.AllocRegister());
		this.Writer.WriteOpCode(OPCODE_NSET, Reg, NSET_UserConstant, Index);
		this.PushRegister(Reg);
	}

	@Override public void VisitFloatNode(GtFloatNode Node) {
		/*local*/int Index = this.AddConstant(new Double(Node.Value), Node.Type);
		/*local*/int Reg = this.ToUnboxReg(this.AllocRegister());
		this.Writer.WriteOpCode(OPCODE_NSET, Reg, NSET_UserConstant, Index);
		this.PushRegister(Reg);
	}

	@Override public void VisitStringNode(GtStringNode Node) {
		/*local*/int Index = this.AddConstant(Node.Value, Node.Type);
		/*local*/int Reg = this.AllocRegister();
		this.Writer.WriteOpCode(OPCODE_NSET, Reg, NSET_UserConstant, Index);
		this.PushRegister(Reg);
	}

	@Override public void VisitRegexNode(GtRegexNode Node) {
		/*local*/int Index = this.AddConstant(Node.Value, Node.Type);
		/*local*/int Reg = this.AllocRegister();
		this.Writer.WriteOpCode(OPCODE_NSET, Reg, NSET_UserConstant, Index);
		this.PushRegister(Reg);
	}

	@Override public void VisitConstPoolNode(GtConstPoolNode Node) {
		/*local*/int Index = this.AddConstant(Node.ConstValue, Node.Type);
		/*local*/int Reg = this.AllocRegister();
		this.Writer.WriteOpCode(OPCODE_NSET, Reg, NSET_UserConstant, Index);
		this.PushRegister(Reg);
	}

	@Override public void VisitArrayLiteralNode(GtArrayLiteralNode Node) {
		/*local*/int ArraySize = LibGreenTea.ListSize(Node.NodeList);
		/*local*/int TargetReg = this.ReserveRegister(ArraySize + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		//this.Writer.WriteOpCode(OPCODE_NEW, CallReg, TypeSource_Default, );
		for(/*local*/int i = 0; i < ArraySize; ++i) {
			Node.NodeList.get(i).Accept(this);
			this.WriteOPNMOV(CallReg+(i+1)*2, this.PopRegister());
		}
		this.WriteInternalMethodCall(CallReg, ArraySize, Array_newList);
	}

	@Override public void VisitMapLiteralNode(GtMapLiteralNode Node) {
		/*FIXME*/
	}

	@Override public void VisitParamNode(GtParamNode Node) {
		/*FIXME*/
	}

	@Override public void VisitFunctionLiteralNode(GtFunctionLiteralNode Node) {
		/*FIXME*/
	}

	@Override public void VisitGetLocalNode(GtGetLocalNode Node) {
		this.PushRegister(this.LocalVarMap.get(Node.NativeName));
	}

	@Override public void VisitSetLocalNode(GtSetLocalNode Node) {
		Node.ValueNode.Accept(this);
		this.WriteOPNMOV(this.LocalVarMap.get(Node.NativeName), this.PopRegister());
	}

	@Override public void VisitGetCapturedNode(GtGetCapturedNode Node) {
		/*FIXME*/
	}

	@Override public void VisitSetCapturedNode(GtSetCapturedNode Node) {
		/*FIXME*/
	}

	@Override public void VisitGetterNode(GtGetterNode Node) {
		Node.RecvNode.Accept(this);
		/*local*/int TargetReg = this.AllocRegister();
		/*local*/ArrayList<GtFieldInfo> FieldList = this.ClassFieldMap.get(Node.RecvNode.Type);
		/*local*/int Offset = this.GetFieldOffset(FieldList, Node.NativeName);
		this.Writer.WriteOpCode(OPCODE_NMOVx, TargetReg, this.PopRegister(), Offset); //FIXME
		this.PushRegister(TargetReg); //FIXME
	}

	@Override public void VisitSetterNode(GtSetterNode Node) {
		Node.RecvNode.Accept(this);
		/*local*/int TargetReg = this.PopRegister();
		/*local*/ArrayList<GtFieldInfo> FieldList = this.ClassFieldMap.get(Node.RecvNode.Type);
		/*local*/int Offset = this.GetFieldOffset(FieldList, Node.NativeName);
		Node.ValueNode.Accept(this);
		this.Writer.WriteOpCode(OPCODE_XNMOV, TargetReg, Offset, this.PopRegister());
	}

	@Override public void VisitApplySymbolNode(GtApplySymbolNode Node) {
		/*local*/int ParamSize = LibGreenTea.ListSize(Node.ParamList);
		/*local*/int TargetReg = this.ReserveRegister(ParamSize + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		if(this.IsUnboxType(Node.ResolvedFunc.Types[0]/*Return Type*/)) {
			TargetReg = this.ToUnboxReg(TargetReg);
		}
		for(/*local*/int i = 0; i < ParamSize; ++i) {
			Node.ParamList.get(i).Accept(this);
			this.WriteOPNMOV(CallReg+(i+1)*2, this.PopRegister());
		}
		/*local*/int CallMethod = this.MethodPool.indexOf(Node.ResolvedFunc);
		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+MethodIndex), NSET_UserMethod, CallMethod);
		this.Writer.WriteOpCode(OPCODE_CALL, CallReg, CallReg+ParamSize*2+2, 0);
		if(Node.ResolvedFunc.Types[0].IsVoidType()) {
			this.FreeRegister(TargetReg);
		}
		else {
			this.PushRegister(TargetReg);
			this.FreeRegister(this.ToBoxReg(TargetReg + 2));
		}
	}

	@Override public void VisitApplyFunctionObjectNode(GtApplyFunctionObjectNode Node) {
		/*FIXME*/
		/*local*/int ParamSize = LibGreenTea.ListSize(Node.ParamList);
		/*local*/int TargetReg = this.ReserveRegister(ParamSize + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		for(/*local*/int i = 0; i < ParamSize; ++i) {
			Node.ParamList.get(i).Accept(this);
			this.WriteOPNMOV(CallReg+(i+1)*2, this.PopRegister());
		}
		Node.FuncNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		this.Writer.WriteOpCode(OPCODE_LOOKUP, CallReg, 0, 0);
		this.Writer.WriteOpCode(OPCODE_CALL, CallReg, CallReg+ParamSize*2+2, 0);
		this.PushRegister(TargetReg); //FIXME
		this.FreeRegister(TargetReg + 2);
	}

	@Override public void VisitApplyOverridedMethodNode(GtApplyOverridedMethodNode Node) {
		/*FIXME*/
	}

	@Override public void VisitGetIndexNode(GtGetIndexNode Node) {
		/*local*/int TargetReg = this.ReserveRegister(1/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		Node.IndexNode.Accept(this);
		this.WriteOPNMOV(CallReg+2, this.PopRegister());
		this.WriteInternalMethodCall(CallReg, 1/*ArgumentSize*/, Array_get);
	}

	@Override public void VisitSetIndexNode(GtSetIndexNode Node) {
		/*local*/int TargetReg = this.ReserveRegister(2/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		///*local*/int ArrayVarReg = this.PopRegister();
		//this.WriteOPNMOV(CallReg+2, ArrayVarReg);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		Node.IndexNode.Accept(this);
		this.WriteOPNMOV(CallReg+2, this.PopRegister());
		Node.ValueNode.Accept(this);
		this.WriteOPNMOV(CallReg+4, this.PopRegister());
		this.WriteInternalMethodCall(CallReg, 2/*ArgumentSize*/, Array_set);
	}

	@Override public void VisitSliceNode(GtSliceNode Node) {
		/*FIXME*/
	}

	@Override public void VisitAndNode(GtAndNode Node) {
		/*local*/int TargetReg = this.ToUnboxReg(this.AllocRegister());
		/*local*/int EndLabel = this.NewLabel();
		Node.LeftNode.Accept(this);
		this.WriteOPNMOV(TargetReg, this.PopRegister());
		this.Writer.WriteOpCode(OPCODE_JMPF, EndLabel, TargetReg, 0);
		Node.RightNode.Accept(this);
		this.WriteOPNMOV(TargetReg, this.PopRegister());
		this.Writer.WriteNewLabel(EndLabel);
		this.PushRegister(TargetReg);
	}

	@Override public void VisitOrNode(GtOrNode Node) {
		/*local*/int TargetReg = this.ToUnboxReg(this.AllocRegister());
		/*local*/int RightLabel = this.NewLabel();
		/*local*/int EndLabel = this.NewLabel();
		Node.LeftNode.Accept(this);
		this.WriteOPNMOV(TargetReg, this.PopRegister());
		this.Writer.WriteOpCode(OPCODE_JMPF, RightLabel, TargetReg, 0);
		this.Writer.WriteOpCode(OPCODE_JMP, EndLabel, 0, 0);
		this.Writer.WriteNewLabel(RightLabel);
		Node.RightNode.Accept(this);
		this.WriteOPNMOV(TargetReg, this.PopRegister());
		this.Writer.WriteNewLabel(EndLabel);
		this.PushRegister(TargetReg);
	}

	@Override public void VisitUnaryNode(GtUnaryNode Node) {
		/*local*/int TargetReg = this.ReserveRegister(0/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		this.WriteOPNMOV(CallReg+2, this.PopRegister());
		/*local*/String Op = Node.Token.ParsedText; //Node.NativeName
		this.WriteInternalMethodCall(CallReg, 0/*ArgumentSize*/, this.GetMethodByUnary(Op, Node.RecvNode.Type));
	}

	@Override public void VisitPrefixInclNode(GtPrefixInclNode Node) {
		/*local*/int TargetReg = this.ReserveRegister(1/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		/*local*/int Int1_Index = this.AddConstant(new Long(1), GtStaticTable.IntType);
		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+2), NSET_UserConstant, Int1_Index);
		this.WriteInternalMethodCall(CallReg, 1/*ArgumentSize*/, Int_opADD);
	}

	@Override public void VisitPrefixDeclNode(GtPrefixDeclNode Node) {
		/*local*/int TargetReg = this.ReserveRegister(1/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		/*local*/int Int1_Index = this.AddConstant(new Long(1), GtStaticTable.IntType);
		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+2), NSET_UserConstant, Int1_Index);
		this.WriteInternalMethodCall(CallReg, 1/*ArgumentSize*/, Int_opSUB);
	}

	@Override public void VisitSuffixInclNode(GtSuffixInclNode Node) {
		/*local*/int TargetReg = this.ToUnboxReg(this.AllocRegister());
		/*local*/int ReturnReg = this.ReserveRegister(1/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = ReturnReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		/*local*/int Int1_Index = this.AddConstant(new Long(1), GtStaticTable.IntType);
		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+2), NSET_UserConstant, Int1_Index);
		this.WriteInternalMethodCall(CallReg, 1/*ArgumentSize*/, Int_opADD);
		this.PopRegister();
		this.FreeRegister(ReturnReg);
		this.PushRegister(TargetReg);
	}

	@Override public void VisitSuffixDeclNode(GtSuffixDeclNode Node) {
		/*local*/int TargetReg = this.ToUnboxReg(this.AllocRegister());
		/*local*/int ReturnReg = this.ReserveRegister(1/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = ReturnReg - ReturnIndex + ThisIndex;
		Node.RecvNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		/*local*/int Int1_Index = this.AddConstant(new Long(1), GtStaticTable.IntType);
		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+2), NSET_UserConstant, Int1_Index);
		this.WriteInternalMethodCall(CallReg, 1/*ArgumentSize*/, Int_opSUB);
		this.PopRegister();
		this.FreeRegister(ReturnReg);
		this.PushRegister(TargetReg);
	}

	@Override public void VisitBinaryNode(GtBinaryNode Node) {
		/*local*/int TargetReg = this.ReserveRegister(1/*ArgumentSize*/ + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		Node.LeftNode.Accept(this);
		this.WriteOPNMOV(CallReg, this.PopRegister());
		Node.RightNode.Accept(this);
		this.WriteOPNMOV(CallReg+2, this.PopRegister());
		/*local*/String Op = Node.Token.ParsedText; //Node.NativeName
		this.WriteInternalMethodCall(CallReg, 1/*ArgumentSize*/, this.GetMethodByBinary(Op, Node.LeftNode.Type));
	}

	@Override public void VisitTrinaryNode(GtTrinaryNode Node) {
		/*FIXME*/
	}

	@Override public void VisitConstructorNode(GtConstructorNode Node) {
		/*local*/int ParamSize = LibGreenTea.ListSize(Node.ParamList);
		/*local*/int TargetReg = this.ReserveRegister(ParamSize + CallParameters);
		/*local*/int CallReg = TargetReg - ReturnIndex + ThisIndex;
		for(/*local*/int i = 0; i < ParamSize; ++i) {
			Node.ParamList.get(i).Accept(this);
			this.WriteOPNMOV(CallReg+(i+1)*2, this.PopRegister());
		}
		/*local*/int CallMethod = this.MethodPool.indexOf(Node.Func);
		this.Writer.WriteOpCode(OPCODE_NSET, this.ToUnboxReg(CallReg+MethodIndex), NSET_UserMethod, CallMethod);
		//this.Writer.WriteMethodBody("NEW  " + "REG" + CallReg, " CLASS" + this.ClassPool.indexOf(Node.Type));
		this.Writer.WriteOpCode(OPCODE_CALL, CallReg, CallReg+ParamSize*2+2, 0);
		this.PushRegister(TargetReg); //FIXME
		this.FreeRegister(TargetReg + 2);
	}

	@Override public void VisitAllocateNode(GtAllocateNode Node) {
		/*local*/int Reg = this.AllocRegister();
		this.Writer.WriteOpCode(OPCODE_NEW, Reg, TypeSource_UserDefined, this.ClassPool.indexOf(Node.Type));
		this.PushRegister(Reg);
	}

	@Override public void VisitNewArrayNode(GtNewArrayNode Node) {
		/*FIXME*/
	}

	@Override public void VisitInstanceOfNode(GtInstanceOfNode Node) {
		/*FIXME*/
	}

	@Override public void VisitCastNode(GtCastNode Node) {
		/*FIXME*/
	}

	@Override public void VisitVarDeclNode(GtVarDeclNode Node) {
		int VarReg = this.AllocRegister();
		if(this.IsUnboxType(Node.Type)) {
			VarReg = this.ToUnboxReg(VarReg);
		}
		this.LocalVarMap.put(Node.NativeName, VarReg);
		Node.InitNode.Accept(this);
		this.WriteOPNMOV(this.LocalVarMap.get(Node.NativeName), this.PopRegister());
		this.VisitBlock(Node.BlockNode);
		this.LocalVarMap.remove(Node.NativeName);
	}

	@Override public void VisitUsingNode(GtUsingNode Node) {
		/*FIXME*/
	}

	@Override public void VisitIfNode(GtIfNode Node) {
		/*local*/int ElseLabel = this.NewLabel();
		/*local*/int EndLabel = this.NewLabel();
		Node.CondNode.Accept(this);
		this.Writer.WriteOpCode(OPCODE_JMPF, ElseLabel, this.ToUnboxReg(this.PopRegister()), 0);
		this.VisitBlock(Node.ThenNode);
		this.Writer.WriteOpCode(OPCODE_JMP, EndLabel, 0, 0);
		this.Writer.WriteNewLabel(ElseLabel);
		this.VisitBlock(Node.ElseNode);
		this.Writer.WriteNewLabel(EndLabel);
	}

	@Override public void VisitWhileNode(GtWhileNode Node) {
		/*local*/int CondLabel = this.NewLabel();
		/*local*/int EndLabel = this.NewLabel();
		this.PushLoopLabel(CondLabel, EndLabel);
		this.Writer.WriteNewLabel(CondLabel);
		Node.CondNode.Accept(this);
		this.Writer.WriteOpCode(OPCODE_JMPF, EndLabel, this.ToUnboxReg(this.PopRegister()), 0);
		this.VisitBlock(Node.BodyNode);
		this.Writer.WriteOpCode(OPCODE_JMP, CondLabel, 0, 0);
		this.Writer.WriteNewLabel(EndLabel);
		this.PopLoopLabel();
	}

	@Override public void VisitDoWhileNode(GtDoWhileNode Node) {
		/*local*/int BodyLabel = this.NewLabel();
		/*local*/int CondLabel = this.NewLabel();
		/*local*/int EndLabel = this.NewLabel();
		this.PushLoopLabel(CondLabel, EndLabel);
		this.Writer.WriteNewLabel(BodyLabel);
		this.VisitBlock(Node.BodyNode);
		this.Writer.WriteNewLabel(CondLabel);
		Node.CondNode.Accept(this);
		this.Writer.WriteOpCode(OPCODE_JMPF, EndLabel, this.ToUnboxReg(this.PopRegister()), 0);
		this.Writer.WriteOpCode(OPCODE_JMP, BodyLabel, 0, 0);
		this.Writer.WriteNewLabel(EndLabel);
		this.PopLoopLabel();
	}

	@Override public void VisitForNode(GtForNode Node) {
		/*local*/int CondLabel = this.NewLabel();
		/*local*/int IterLabel = this.NewLabel();
		/*local*/int EndLabel = this.NewLabel();
		this.PushLoopLabel(IterLabel, EndLabel);
		this.Writer.WriteNewLabel(CondLabel);
		Node.CondNode.Accept(this);
		this.Writer.WriteOpCode(OPCODE_JMPF, EndLabel, this.ToUnboxReg(this.PopRegister()), 0);
		this.VisitBlock(Node.BodyNode);
		this.Writer.WriteNewLabel(IterLabel);
		this.VisitBlock(Node.IterNode);
		this.Writer.WriteOpCode(OPCODE_JMP, CondLabel, 0, 0);
		this.Writer.WriteNewLabel(EndLabel);
		this.PopLoopLabel();
	}

	@Override public void VisitForEachNode(GtForEachNode Node) {
		/*FIXME*/
	}

	@Override public void VisitContinueNode(GtContinueNode Node) {
		this.Writer.WriteOpCode(OPCODE_JMP, this.PeekContinueLabel(), 0, 0);
	}

	@Override public void VisitBreakNode(GtBreakNode Node) {
		this.Writer.WriteOpCode(OPCODE_JMP, this.PeekBreakLabel(), 0, 0);
	}

	@Override public void VisitStatementNode(GtStatementNode Node) {
		/*FIXME*/
	}

	@Override public void VisitReturnNode(GtReturnNode Node) {
		if(Node.ValueNode != null) {
			Node.ValueNode.Accept(this);
			this.WriteOPNMOV(ReturnIndex, this.PopRegister());
		}
		this.Writer.WriteOpCode(OPCODE_RET, 0, 0, 0);
	}

	@Override public void VisitYieldNode(GtYieldNode Node) {
		/*FIXME*/
	}

	@Override public void VisitThrowNode(GtThrowNode Node) {
		/*FIXME*/
	}

	@Override public void VisitTryNode(GtTryNode Node) {
		/*FIXME*/
	}

	@Override public void VisitCatchNode(GtCatchNode Node) {
		/*FIXME*/
	}

	@Override public void VisitSwitchNode(GtSwitchNode Node) {
		/*FIXME*/
	}

	@Override public void VisitCaseNode(GtCaseNode Node) {
		/*FIXME*/
	}

	@Override public void VisitCommandNode(GtCommandNode Node) {
		/*FIXME*/
	}

	@Override public void VisitErrorNode(GtErrorNode Node) {
		/*FIXME*/
	}

	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
		this.AddMethod(Func);
		/*local*/String MethodName = Func.GetNativeFuncName();
		this.RegisterNum = ThisIndex + 2;
		//this.LabelNum = 0;
		this.Writer.CreateNewMethodBody();
		/*local*/int ParamSize = LibGreenTea.ListSize(ParamNameList);
		///*local*/HashMap<String,Integer> PushedMap = (/*cast*/HashMap<String,Integer>)this.LocalVarMap.clone();
		for(/*local*/int i = 0; i < ParamSize; ++i) {
			/*local*/String ParamName = ParamNameList.get(i);
			/*local*/GtType ParamType = Func.Types[i+1];
			int ArgReg = this.AllocRegister();
			if(this.IsUnboxType(ParamType)) {
				ArgReg = this.ToUnboxReg(ArgReg);
			}
			this.LocalVarMap.put(ParamName, ArgReg);
			/*local*/int ParamTypeNum = this.GetDefaultTypeNumber(ParamType);
			if(ParamTypeNum != -1) {
				this.Writer.WriteMethodParam(TypeSource_Default, ParamTypeNum);
			}
			else {
				this.Writer.WriteMethodParam(TypeSource_UserDefined, this.ClassPool.indexOf(ParamType));
			}
		}
		this.Writer.WriteOpCode(OPCODE_CHKSTACK, 0, 0, 0);
		this.VisitBlock(Body);
		/*local*/GtType ReturnType = Func.Types[0];
		/*local*/int ReturnTypeNum = this.GetDefaultTypeNumber(ReturnType);
		if(ReturnTypeNum != -1) {
			this.Writer.SetNewMethod(TypeSource_Default, ReturnTypeNum, MethodName);
		}
		else {
			this.Writer.SetNewMethod(TypeSource_UserDefined, this.ClassPool.indexOf(ReturnType), MethodName);
		}
		//this.LocalVarMap = PushedMap;
	}

	@Override public void OpenClassField(GtSyntaxTree ParsedTree, GtType Type, GtClassField ClassField) {
		this.ClassPool.add(Type);
		this.ClassFieldMap.put(Type, ClassField.FieldList);
		
		this.Writer.CreateNewClassField();
		/*local*/int FieldSize = ClassField.FieldList.size();
		for(/*local*/int i = 0; i < FieldSize; ++i) {
			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
			/*local*/int DefaultTypeNum = this.GetDefaultTypeNumber(FieldInfo.Type);
			if(DefaultTypeNum != -1) {
				this.Writer.WriteClassField(TypeSource_Default, DefaultTypeNum, FieldInfo.NativeName);
			}
			else {
				this.Writer.WriteClassField(TypeSource_UserDefined, this.ClassPool.indexOf(Type), FieldInfo.NativeName);
			}
		}
		this.Writer.SetNewClass(Type.ShortName);
	}
	
	@Override public void InvokeMainFunc(String MainFuncName) {
		/*FIXME*/
	}
}
