// ***************************************************************************
// Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

//ifdef JAVA
package org.GreenTeaScript;
import java.util.ArrayList;
//endif VAJA

class GtNode extends GreenTeaUtils {
	/*field*/public GtNode	ParentNode;
	/*field*/public GtNode	PrevNode;
	/*field*/public GtNode	NextNode;
	/*field*/public GtType	Type;
	/*field*/public GtToken	Token;
	GtNode/*constructor*/(GtType Type, GtToken Token) {
		this.Type = Type;
		this.Token = Token;
		this.ParentNode = null;
		this.PrevNode = null;
		this.NextNode = null;
	}
//	public final GtNode GetParentNode() {
//		return this.ParentNode;
//	}
	public final void SetChild(GtNode Node) {
		if(Node != null) {
			Node.ParentNode = this;
		}
	}
	public final void SetChild2(GtNode Node, GtNode Node2) {
		this.SetChild(Node);
		this.SetChild(Node2);
	}
//	public final GtNode GetNextNode() {
//		return this.NextNode;
//	}
//	public final void SetNextNode(GtNode Node) {
//		this.NextNode = Node;
//	}
//	public final GtNode GetPrevNode() {
//		return this.PrevNode;
//	}
//	public final void SetPrevNode(GtNode Node) {
//		this.PrevNode = Node;
//	}
	public final GtNode MoveHeadNode() {
		/*local*/GtNode Node = this;
		while(Node.PrevNode != null) {
			Node = Node.PrevNode;
		}
		return Node;
	}
	public final GtNode MoveTailNode() {
		/*local*/GtNode Node = this;
		while(Node.NextNode != null) {
			Node = Node.NextNode;
		}
		return Node;
	}
	
	public ArrayList<GtNode> GetList() {
		return null;
	}
	public final GtNode GetAt(int Index) {
		return this.GetList().get(Index);
	}
	public final void Append(GtNode Node) {
		this.GetList().add(Node);
		this.SetChild(Node);
	}
	public final void AppendNodeList(int StartIndex, ArrayList<GtNode> NodeList) {
		/*local*/int i = StartIndex;
		/*local*/ArrayList<GtNode> List = this.GetList();
		while(i < LibGreenTea.ListSize(NodeList)) {
			/*local*/GtNode Node = NodeList.get(i);
			List.add(Node);
			this.SetChild(Node);
			i = i + 1;
		}
	}

	public void Evaluate(GtGenerator Visitor) {
		/* must override */
	}


	public final boolean IsErrorNode() {
		return (this instanceof GtErrorNode);
	}
	
	public final boolean IsNullNode() {
		return (this instanceof GtNullNode);
	}

	protected final Object ToNullValue(boolean EnforceConst) {
		if(EnforceConst) {
			this.Type.Context.ReportError(ErrorLevel, this.Token, "value must be constant in this context");
		}
		return null;
	}
	public Object ToConstValue(boolean EnforceConst)  {
		return this.ToNullValue(EnforceConst);
	}

}

final class GtEmptyNode extends GtNode {
	GtEmptyNode/*constructor*/(GtType Type, GtToken Token) {
		super(Type, Token);
	}
	public Object ToConstValue(boolean EnforceConst)  {
		return null;
	}
}

final class GtNullNode extends GtNode {
	GtNullNode/*constructor*/(GtType Type, GtToken Token) {
		super(Type, Token);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitNullNode(this);
	}
	public Object ToConstValue(boolean EnforceConst)  {
		return null;
	}
}
//NewNode is object creation in GreenTea defined
final class GtNewNode extends GtNode {
	GtNewNode/*constructor*/(GtType Type, GtToken Token) {
		super(Type, Token);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitNewNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		return this.Type.Context.Generator.EvalNewNode(this, EnforceConst);
	}	
}
final class GtConstNode extends GtNode {
	/*field*/public Object	ConstValue;
	GtConstNode/*constructor*/(GtType Type, GtToken Token, Object ConstValue) {
		super(Type, Token);
		this.ConstValue = ConstValue;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitConstNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		return this.ConstValue;
	}
}
//E.g., "[" $Node, $Node "]"
final class GtArrayNode extends GtNode {
	/*field*/public ArrayList<GtNode>	NodeList;
	GtArrayNode/*constructor*/(GtType Type, GtToken Token) {
		super(Type, Token);
		this.NodeList = new ArrayList<GtNode>();
	}
	@Override public ArrayList<GtNode> GetList() {
		return this.NodeList;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitArrayNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		if(EnforceConst) {
			return this.Type.Context.Generator.EvalArrayNode(this, EnforceConst);
		}
		return null;
	}
}
final class GtLocalNode extends GtNode {
	/*field*/public String NativeName;
	GtLocalNode/*constructor*/(GtType Type, GtToken Token, String NativeName) {
		super(Type, Token);
		this.NativeName = NativeName;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitLocalNode(this);
	}
}
//E.g., $LeftNode = $RightNode
final class GtAssignNode extends GtNode {
	/*field*/public GtNode   LeftNode;
	/*field*/public GtNode	 RightNode;
	GtAssignNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
		super(Type, Token);
		this.LeftNode  = Left;
		this.RightNode = Right;
		this.SetChild2(Left, Right);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitAssignNode(this);
	}
}
//E.g., $ExprNode instanceof TypeInfo
final class GtInstanceOfNode extends GtNode {
	/*field*/public GtNode   ExprNode;
	/*field*/public GtType	 TypeInfo;
	GtInstanceOfNode/*constructor*/(GtType Type, GtToken Token, GtNode ExprNode, GtType TypeInfo) {
		super(Type, Token);
		this.ExprNode = ExprNode;
		this.TypeInfo = TypeInfo;
		this.SetChild(ExprNode);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitInstanceOfNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object Value = this.ExprNode.ToConstValue(EnforceConst) ;
		if(Value != null) {
			return LibGreenTea.DynamicInstanceOf(Value, this.TypeInfo);
		}
		return Value;
	}
}

//E.g., $LeftNode && $RightNode
final class GtAndNode extends GtNode {
	/*field*/public GtNode   LeftNode;
	/*field*/public GtNode	 RightNode;
	GtAndNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
		super(Type, Token);
		this.LeftNode  = Left;
		this.RightNode = Right;
		this.SetChild2(Left, Right);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitAndNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object LeftValue = this.LeftNode.ToConstValue(EnforceConst) ;
//		if(LeftValue instanceof Boolean && LibGreenTea.booleanValue(LeftValue)) {
//			return this.RightNode.ToConstValue(EnforceConst) ;
//		}
		return null;
	}
}
//E.g., $LeftNode || $RightNode
final class GtOrNode extends GtNode {
	/*field*/public GtNode   LeftNode;
	/*field*/public GtNode	 RightNode;
	GtOrNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
		super(Type, Token);
		this.LeftNode  = Left;
		this.RightNode = Right;
		this.SetChild2(Left, Right);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitOrNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object LeftValue = this.LeftNode.ToConstValue(EnforceConst) ;
		if(LeftValue instanceof Boolean) {
//			if(LibGreenTea.booleanValue(LeftValue)) {
//				return LeftValue;
//			}
//			else {
//				return this.RightNode.ToConstValue(EnforceConst) ;
//			}
		}
		return null;
	}
}

final class GtVarNode extends GtNode {
	/*field*/public GtType	DeclType;
//	/*field*/public GtNode	VarNode;
	/*field*/public String  NativeName;
	/*field*/public GtNode	InitNode;
	/*field*/public GtNode	BlockNode;
	/* let VarNode in Block end */
	GtVarNode/*constructor*/(GtType Type, GtToken Token, GtType DeclType, String VariableName, GtNode InitNode, GtNode Block) {
		super(Type, Token);
		this.NativeName = VariableName;
		this.DeclType  = DeclType;
		this.InitNode  = InitNode;
		this.BlockNode = Block;
		this.SetChild2(InitNode, this.BlockNode);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitVarNode(this);
	}
}

//E.g., (T) $Expr
final class GtCastNode extends GtNode {
	/*field*/public GtFunc  Func;
	/*field*/public GtType	CastType;
	/*field*/public GtNode	Expr;
	GtCastNode/*constructor*/(GtType Type, GtToken Token, GtType CastType, GtNode Expr) {
		super(Type, Token);
		this.CastType = CastType;
		this.Expr = Expr;
		this.SetChild(Expr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitCastNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
		if(Value != null) {
			return LibGreenTea.DynamicCast(this.CastType, Value);
		}
		return Value;
	}
}
// E.g., "~" $Expr
final class GtUnaryNode extends GtNode {
	/*field*/public GtFunc  Func;
	/*field*/public GtNode	Expr;
	GtUnaryNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
		super(Type, Token);
		this.Func = Func;
		this.Expr = Expr;
		this.SetChild(Expr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitUnaryNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
		if(Value != null) {
			return LibGreenTea.EvalUnary(this.Type, this.Token.ParsedText, Value);
		}
		return Value;
	}	
}
// E.g.,  $Expr "++"
final class GtSuffixNode extends GtNode {
	/*field*/public GtFunc  Func;
	/*field*/public GtNode	Expr;
	GtSuffixNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
		super(Type, Token);
		this.Func = Func;
		this.Expr = Expr;
		this.SetChild(Expr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitSuffixNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
		if(Value != null) {
			return LibGreenTea.EvalSuffix(this.Type, Value, this.Token.ParsedText);
		}
		return Value;
	}
}
//E.g., "exists" $Expr
final class GtExistsNode extends GtNode {
	/*field*/public GtFunc  Func;
	/*field*/public GtNode	Expr;
	GtExistsNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
		super(Type, Token);
		this.Func = Func;
		this.Expr = Expr;
		this.SetChild(Expr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitExistsNode(this);
	}
}
//E.g., $LeftNode += $RightNode
final class GtSelfAssignNode extends GtNode {
	/*field*/public GtFunc Func;
	/*field*/public GtNode LeftNode;
	/*field*/public GtNode RightNode;
	GtSelfAssignNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Left, GtNode Right) {
		super(Type, Token);
		this.Func  = Func;
		this.LeftNode  = Left;
		this.RightNode = Right;
		this.SetChild2(Left, Right);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitSelfAssignNode(this);
	}
}
// E.g., $LeftNode "+" $RightNode
final class GtBinaryNode extends GtNode {
	/*field*/public GtFunc    Func;
	/*field*/public GtNode    LeftNode;
	/*field*/public GtNode	  RightNode;
	GtBinaryNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Left, GtNode Right) {
		super(Type, Token);
		this.Func = Func;
		this.LeftNode  = Left;
		this.RightNode = Right;
		this.SetChild2(Left, Right);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitBinaryNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object LeftValue = this.LeftNode.ToConstValue(EnforceConst) ;
		if(LeftValue != null) {
			/*local*/Object RightValue = this.RightNode.ToConstValue(EnforceConst) ;
			if(RightValue != null) {
				return LibGreenTea.EvalBinary(this.Type, LeftValue, this.Token.ParsedText, RightValue);
			}
		}
		return null;
	}
}


//E.g., $CondExpr "?" $ThenExpr ":" $ElseExpr
final class GtTrinaryNode extends GtNode {
	/*field*/public GtFunc  Func;
	/*field*/public GtNode	CondExpr;
	/*field*/public GtNode	ThenExpr;
	/*field*/public GtNode	ElseExpr;
	GtTrinaryNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode ThenExpr, GtNode ElseExpr) {
		super(Type, Token);
		this.CondExpr = CondExpr;
		this.ThenExpr = ThenExpr;
		this.ElseExpr = ElseExpr;
		this.SetChild(CondExpr);
		this.SetChild2(ThenExpr, ElseExpr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitTrinaryNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object CondValue = this.CondExpr.ToConstValue(EnforceConst) ;
		if(CondValue instanceof Boolean) {
//			if(LibGreenTea.booleanValue(CondValue)) {
//				return this.ThenExpr.ToConstValue(EnforceConst) ;
//			}
//			else {
//				return this.ElseExpr.ToConstValue(EnforceConst) ;
//			}
		}
		return null;
	}
}
//E.g., $Expr . Token.ParsedText
final class GtGetterNode extends GtNode {
	/*field*/public GtNode Expr;
	/*field*/public GtFunc  Func;
	GtGetterNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
		super(Type, Token);
		this.Func = Func;
		this.Expr = Expr;
		this.SetChild(Expr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitGetterNode(this);
	}

	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
		if(Value != null) {
			return LibGreenTea.EvalGetter(this.Type, Value, this.Token.ParsedText);
		}
		return Value;
	}
}
//E.g., $Expr "[" $Node, $Node "]"
final class GtIndexerNode extends GtNode {
	/*field*/public GtFunc Func;
	/*field*/public GtNode Expr;
	/*field*/public ArrayList<GtNode>  NodeList; /* [arg1, arg2, ...] */
	GtIndexerNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
		super(Type, Token);
		this.Func = Func;
		this.Expr = Expr;
		this.NodeList = new ArrayList<GtNode>();
		this.SetChild(Expr);
	}
	public ArrayList<GtNode> GetList() {
		return this.NodeList;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitIndexerNode(this);
	}
	public GtApplyNode ToApplyNode() {
		/*local*/GtApplyNode Node = new GtApplyNode(this.Type, this.Token, this.Func);
		Node.Append(new GtConstNode(this.Func.GetFuncType(), this.Token, this.Func));
		Node.Append(this.Expr);
		Node.AppendNodeList(0, this.NodeList);
		return Node;
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		/*local*/GtApplyNode Node = this.ToApplyNode();
		return Node.ToConstValue(EnforceConst);
	}
}

//E.g., $Expr "[" $Index ":" $Index2 "]"
final class GtSliceNode extends GtNode {
	/*field*/public GtFunc  Func;
	/*field*/public GtNode Expr;
	/*field*/public GtNode Index1;
	/*field*/public GtNode Index2;
	GtSliceNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr, GtNode Index1, GtNode Index2) {
		super(Type, Token);
		this.Func = Func;
		this.Expr = Expr;
		this.Index1 = Index1;
		this.Index2 = Index2;
		this.SetChild(Expr);
		this.SetChild2(Index1, Index2);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitSliceNode(this);
	}
}

// E.g., $Param[0] "(" $Param[1], $Param[2], ... ")"
final class GtApplyNode extends GtNode {
	/*field*/public GtFunc	Func;
	/*field*/public ArrayList<GtNode>  NodeList; /* [arg1, arg2, ...] */
	GtApplyNode/*constructor*/(GtType Type, GtToken KeyToken, GtFunc Func) {
		super(Type, KeyToken);
		this.Func = Func;
		this.NodeList = new ArrayList<GtNode>();
	}
	public final ArrayList<GtNode> GetList() {
		return this.NodeList;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitApplyNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		return this.Type.Context.Generator.EvalApplyNode(this, EnforceConst);
	}
}

//E.g., ConstructorNode is for object creation in Native Langauage defined
final class GtConstructorNode extends GtNode {
	/*field*/public ArrayList<GtNode>	ParamList;
	/*field*/GtFunc Func;
	GtConstructorNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func) {
		super(Type, Token);
		this.ParamList = new ArrayList<GtNode>();
		this.Func = Func;
	}
	public final ArrayList<GtNode> GetList() {
		return this.ParamList;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitConstructorNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		if(EnforceConst) {
			return this.Type.Context.Generator.EvalConstructorNode(this, EnforceConst);
		}
		return null;
	}	
}

//E.g., "if" "(" $Cond ")" $ThenNode "else" $ElseNode
final class GtIfNode extends GtNode {
	/*field*/public GtNode	CondExpr;
	/*field*/public GtNode	ThenNode;
	/*field*/public GtNode	ElseNode;
	/* If CondExpr then ThenBlock else ElseBlock */
	GtIfNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode ThenBlock, GtNode ElseNode) {
		super(Type, Token);
		this.CondExpr = CondExpr;
		this.ThenNode = ThenBlock;
		this.ElseNode = ElseNode;
		this.SetChild(CondExpr);
		this.SetChild2(ThenBlock, ElseNode);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitIfNode(this);
	}
}
//E.g., "while" "(" $CondExpr ")" $LoopBody
final class GtWhileNode extends GtNode {
	/*field*/public GtNode	CondExpr;
	/*field*/public GtNode	LoopBody;
	GtWhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode LoopBody) {
		super(Type, Token);
		this.CondExpr = CondExpr;
		this.LoopBody = LoopBody;
		this.SetChild2(CondExpr, LoopBody);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitWhileNode(this);
	}
}
final class GtDoWhileNode extends GtNode {
	/*field*/public GtNode	CondExpr;
	/*field*/public GtNode	LoopBody;
	GtDoWhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode LoopBody) {
		super(Type, Token);
		this.CondExpr = CondExpr;
		this.LoopBody = LoopBody;
		this.SetChild2(CondExpr, LoopBody);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitDoWhileNode(this);
	}
}

//E.g., "for" "(" ";" $CondExpr ";" $IterExpr ")" $LoopNode
final class GtForNode extends GtNode {
	/*field*/public GtNode	CondExpr;
	/*field*/public GtNode	IterExpr;
	/*field*/public GtNode	LoopBody;
	GtForNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode IterExpr, GtNode LoopBody) {
		super(Type, Token);
		this.CondExpr = CondExpr;
		this.LoopBody = LoopBody;
		this.IterExpr = IterExpr;
		this.SetChild2(CondExpr, LoopBody);
		this.SetChild(IterExpr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitForNode(this);
	}
}

//E.g., "for" "(" $Variable ":" $IterExpr ")" $LoopNode
final class GtForEachNode extends GtNode {
	/*field*/public GtNode	Variable;
	/*field*/public GtNode	IterExpr;
	/*field*/public GtNode	LoopBody;
	GtForEachNode/*constructor*/(GtType Type, GtToken Token, GtNode Variable, GtNode IterExpr, GtNode LoopBody) {
		super(Type, Token);
		this.Variable = Variable;
		this.IterExpr = IterExpr;
		this.LoopBody = LoopBody;
		this.SetChild2(Variable, LoopBody);
		this.SetChild(IterExpr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitForEachNode(this);
	}
}
final class GtContinueNode extends GtNode {
	/*field*/public String Label;
	GtContinueNode/*constructor*/(GtType Type, GtToken Token, String Label) {
		super(Type, Token);
		this.Label = Label;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitContinueNode(this);
	}
}
final class GtBreakNode extends GtNode {
	/*field*/public String Label;
	GtBreakNode/*constructor*/(GtType Type, GtToken Token, String Label) {
		super(Type, Token);
		this.Label = Label;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitBreakNode(this);
	}
}
final class GtReturnNode extends GtNode {
	/*field*/public GtNode Expr;
	GtReturnNode/*constructor*/(GtType Type, GtToken Token, GtNode Expr) {
		super(Type, Token);
		this.Expr = Expr;
		this.SetChild(Expr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitReturnNode(this);
	}
}
final class GtThrowNode extends GtNode {
	/*field*/public GtNode Expr;
	GtThrowNode/*constructor*/(GtType Type, GtToken Token, GtNode Expr) {
		super(Type, Token);
		this.Expr = Expr;
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitThrowNode(this);
	}
}
final class GtTryNode extends GtNode {
	/*field*/public GtNode	TryBlock;
	/*field*/public GtNode	CatchExpr;
	/*field*/public GtNode	CatchBlock;
	/*field*/public GtNode	FinallyBlock;
	GtTryNode/*constructor*/(GtType Type, GtToken Token, GtNode TryBlock, GtNode CatchExpr, GtNode CatchBlock, GtNode FinallyBlock) {
		super(Type, Token);
		this.TryBlock = TryBlock;
		this.CatchExpr = CatchExpr;
		this.CatchBlock = CatchBlock;
		this.FinallyBlock = FinallyBlock;
		this.SetChild2(TryBlock, FinallyBlock);
		this.SetChild2(CatchBlock, CatchExpr);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitTryNode(this);
	}
}
final class GtSwitchNode extends GtNode {
	/*field*/public GtNode	MatchNode;
	/*field*/public GtNode	DefaultBlock;
	/*field*/public ArrayList<GtNode> CaseList; // [expr, block, expr, block, ....]

	GtSwitchNode/*constructor*/(GtType Type, GtToken Token, GtNode MatchNode, GtNode DefaultBlock) {
		super(Type, Token);
		this.MatchNode = MatchNode;
		this.DefaultBlock = DefaultBlock;
		this.CaseList = new ArrayList<GtNode>();
		this.SetChild(DefaultBlock);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitSwitchNode(this);
	}
	@Override public final ArrayList<GtNode> GetList() {
		return this.CaseList;
	}
}
final class GtFunctionNode extends GtNode {
	GtFunctionNode/*constructor*/(GtType Type, GtToken Token) {
		super(Type, Token); // TODO
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitFunctionNode(this);
	}
}
final class GtErrorNode extends GtNode {
	GtErrorNode/*constructor*/(GtType Type, GtToken Token) {
		super(Type, Token);
	}
	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitErrorNode(this);
	}
	@Override public Object ToConstValue(boolean EnforceConst)  {
		return null;
	}
}

// E.g., "ls" "-a"..
final class GtCommandNode extends GtNode {
	/*field*/public ArrayList<GtNode>  ArgumentList; /* ["/bin/ls" , "-la", "/", ...] */
	/*field*/public GtNode PipedNextNode;
	GtCommandNode/*constructor*/(GtType Type, GtToken KeyToken, GtNode PipedNextNode) {
		super(Type, KeyToken);
		this.PipedNextNode = PipedNextNode;
		this.ArgumentList = new ArrayList<GtNode>();
	}
	@Override public final ArrayList<GtNode> GetList() {
		return this.ArgumentList;
	}

	@Override public void Evaluate(GtGenerator Visitor) {
		Visitor.VisitCommandNode(this);
	}

	@Override public Object ToConstValue(boolean EnforceConst) {	//FIXME: Exception
		return this.Type.Context.Generator.EvalCommandNode(this, EnforceConst);
	}

}


class SourceGenerator extends GtGenerator {
	/*field*/protected String    HeaderSource;
	/*field*/protected String    BodySource;

	/*field*/protected String    Tab;
	/*field*/protected String    LineFeed;
	/*field*/protected int       IndentLevel;
	/*field*/protected String    CurrentLevelIndentString;

	/*field*/protected boolean   HasLabelSupport;
	/*field*/protected String    LogicalOrOperator;
	/*field*/protected String    LogicalAndOperator;
	/*field*/protected String    MemberAccessOperator;
	/*field*/protected String    TrueLiteral;
	/*field*/protected String    FalseLiteral;
	/*field*/protected String    NullLiteral;
	/*field*/protected String    LineComment;
	/*field*/protected String    BreakKeyword;
	/*field*/protected String    ContinueKeyword;
	/*field*/protected String    ParameterBegin;
	/*field*/protected String    ParameterEnd;
	/*field*/protected String    ParameterDelimiter;
	/*field*/protected String    SemiColon;
	/*field*/protected String    BlockBegin;
	/*field*/protected String    BlockEnd;

	SourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
		super(TargetCode, OutputFile, GeneratorFlag);
		this.LineFeed = "\n";
		this.IndentLevel = 0;
		this.Tab = "   ";
		this.CurrentLevelIndentString = null;
		this.HeaderSource = "";
		this.BodySource = "";
		this.HasLabelSupport = false;
		this.LogicalOrOperator  = "||";
		this.LogicalAndOperator = "&&";
		this.MemberAccessOperator = ".";
		this.TrueLiteral  = "true";
		this.FalseLiteral = "false";
		this.NullLiteral  = "null";
		this.BreakKeyword = "break";
		this.ContinueKeyword = "continue";
		this.LineComment  = "//";
		this.ParameterBegin = "(";
		this.ParameterEnd = ")";
		this.ParameterDelimiter = ",";
		this.SemiColon = ";";
		this.BlockBegin = "{";
		this.BlockEnd = "}";
	}

	@Override public void InitContext(GtParserContext Context) {
		super.InitContext(Context);
		this.HeaderSource = "";
		this.BodySource = "";
	}

	public final void WriteHeader(String Text) {
		this.HeaderSource += Text;
	}

	public final void WriteLineHeader(String Text) {
		this.HeaderSource += Text + this.LineFeed;
	}

	public final void WriteCode(String Text) {
		this.BodySource += Text;
	}

	public final void WriteLineCode(String Text) {
		this.BodySource += Text + this.LineFeed;
	}

	public final void WriteLineComment(String Text) {
		this.BodySource += this.LineComment + " " + Text + this.LineFeed;
	}

	public final void FlushErrorReport() {
		this.WriteLineCode("");
		/*local*/String[] Reports = this.Context.GetReportedErrors();
		/*local*/int i = 0;
		while(i < Reports.length) {
			this.WriteLineComment(Reports[i]);
			i = i + 1;
		}
		this.WriteLineCode("");		
	}

	@Override public void FlushBuffer() {
		LibGreenTea.WriteCode(this.OutputFile, this.HeaderSource + this.BodySource);			
		this.HeaderSource = "";
		this.BodySource = "";
	}

	/* GeneratorUtils */

	public final void Indent() {
		this.IndentLevel += 1;
		this.CurrentLevelIndentString = null;
	}

	public final void UnIndent() {
		this.IndentLevel -= 1;
		this.CurrentLevelIndentString = null;
		LibGreenTea.Assert(this.IndentLevel >= 0);
	}

	public final String GetIndentString() {
		if(this.CurrentLevelIndentString == null) {
			this.CurrentLevelIndentString = JoinStrings(this.Tab, this.IndentLevel);
		}
		return this.CurrentLevelIndentString;
	}

	public String VisitBlockWithIndent(GtNode Node, boolean NeedBlock) {
		/*local*/String Code = "";
		if(NeedBlock) {
			Code += this.BlockBegin + this.LineFeed;
			this.Indent();
		}
		/*local*/GtNode CurrentNode = Node;
		while(CurrentNode != null) {
			if(!this.IsEmptyBlock(CurrentNode)) {
				/*local*/String Stmt = this.VisitNode(CurrentNode);
				if(!LibGreenTea.EqualsString(Stmt, "")) {
					Code += this.GetIndentString() + Stmt + this.SemiColon + this.LineFeed;
				}
			}
			CurrentNode = CurrentNode.NextNode;
		}
		if(NeedBlock) {
			this.UnIndent();
			Code += this.GetIndentString() + this.BlockEnd;
		}
//		else if(Code.length() > 0) {
//			Code = Code.substring(0, Code.length() - 1);
//		}
		return Code;
	}

	protected String StringifyConstValue(Object ConstValue) {
		if(ConstValue == null) {
			return this.NullLiteral;
		}
		if(ConstValue instanceof Boolean) {
			if(ConstValue.equals(true)) {
				return this.TrueLiteral;
			}
			else {
				return this.FalseLiteral;
			}
		}
		if(ConstValue instanceof String) {
			return LibGreenTea.QuoteString((/*cast*/String)ConstValue);
		}
		if(ConstValue instanceof GreenTeaEnum) {
			return "" + ((/*cast*/GreenTeaEnum) ConstValue).EnumValue;
		}
		return ConstValue.toString();
	}

	protected String GetNewOperator(GtType Type) {
		return "new " + Type.ShortName + "()";
	}

	protected final void PushSourceCode(String Code) {
		this.PushCode(Code);
	}

	protected final String PopSourceCode() {
		return (/*cast*/String) this.PopCode();
	}

	public final String VisitNode(GtNode Node) {
		Node.Evaluate(this);
		return this.PopSourceCode();
	}

	public final String JoinCode(String BeginCode, int BeginIdx, String[] ParamCode, String EndCode, String Delim) {
		/*local*/String JoinedCode = BeginCode;
		/*local*/int i = BeginIdx;
		while(i < ParamCode.length) {
			/*local*/String P = ParamCode[i];
			if(i != BeginIdx) {
				JoinedCode += Delim;
			}
			JoinedCode += P;
			i = i + 1;
		}
		return JoinedCode + EndCode;
	}

	public final static String GenerateApplyFunc1(GtFunc Func, String FuncName, boolean IsSuffixOp, String Arg1) {
		/*local*/String Macro = null;
		if(Func != null) {
			FuncName = Func.GetNativeFuncName();
			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
				Macro = Func.GetNativeMacro();
			}
		}
		if(Macro == null) {
			if(IsSuffixOp) {
				Macro = "$1 " + FuncName;
			}
			else {
				Macro = FuncName + " $1";
			}
		}
		return Macro.replace("$1", Arg1);
	}

	public final static String GenerateApplyFunc2(GtFunc Func, String FuncName, String Arg1, String Arg2) {
		/*local*/String Macro = null;
		if(Func != null) {
			FuncName = Func.GetNativeFuncName();
			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
				Macro = Func.GetNativeMacro();
			}
		}
		if(Macro == null) {
			Macro = "$1 " + FuncName + " $2";
		}
		return Macro.replace("$1", Arg1).replace("$2", Arg2);
	}

	public String GenerateFuncTemplate(int ParamSize, GtFunc Func) {
		/*local*/int BeginIdx = 1;
		/*local*/String Template = "";
		/*local*/boolean IsNative = false;
		if(Func == null) {
			Template = "$1";
			BeginIdx = 2;
		}
		else if(Func.Is(NativeFunc)) {
			Template = "$1" + this.MemberAccessOperator + Func.FuncName;
			BeginIdx = 2;
		}
		else if(Func.Is(NativeMacroFunc)) {
			Template = Func.GetNativeMacro();
			IsNative = true;
		}
		else {
			Template = Func.GetNativeFuncName();
		}

		if(Func.Is(ConverterFunc)) {
			// T1 converter(FromType, ToType, Value);
			BeginIdx += 1;
		}
		/*local*/int i = BeginIdx;
		if(IsNative == false) {
			Template += this.ParameterBegin;
			while(i < ParamSize) {
				if(i != BeginIdx) {
					Template += this.ParameterDelimiter + " ";
				}
				Template += "$" + i;
				i = i + 1;
			}
			Template += this.ParameterEnd;
		}
		return Template;
	}

	public final String ApplyMacro(String Template, ArrayList<GtNode> NodeList) {
		/*local*/int ParamSize = LibGreenTea.ListSize(NodeList);
		/*local*/int ParamIndex = ParamSize - 1;
		while(ParamIndex >= 1) {
			/*local*/String Param = this.VisitNode(NodeList.get(ParamIndex));
			Template = Template.replace("$" + ParamIndex, Param);
			ParamIndex = ParamIndex - 1;
		}
		return Template;
	}
	public final String ApplyMacro2(String Template, String[] ParamList) {
		/*local*/int ParamSize = ParamList.length;
		/*local*/int ParamIndex = ParamSize - 1;
		while(ParamIndex >= 1) {
			/*local*/String Param = ParamList[ParamIndex];
			Template = Template.replace("$" + ParamIndex, Param);
			ParamIndex = ParamIndex - 1;
		}
		return Template;
	}

	public final String GenerateApplyFunc(GtApplyNode Node) {
		/*local*/int ParamSize = LibGreenTea.ListSize(Node.NodeList);
		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
		return this.ApplyMacro(Template, Node.NodeList);
	}

	// Visitor API
	@Override public void VisitEmptyNode(GtEmptyNode Node) {
		this.PushSourceCode("");
	}

	@Override public void VisitInstanceOfNode(GtInstanceOfNode Node) {
		this.PushSourceCode(this.VisitNode(Node.ExprNode) + " instanceof " + Node.TypeInfo);
	}

	@Override public final void VisitConstNode(GtConstNode Node) {
		this.PushSourceCode(this.StringifyConstValue(Node.ConstValue));
	}

	@Override public final void VisitNullNode(GtNullNode Node) {
		this.PushSourceCode(this.NullLiteral);
	}

	@Override public void VisitLocalNode(GtLocalNode Node) {
		this.PushSourceCode(Node.NativeName);
	}

	@Override public void VisitReturnNode(GtReturnNode Node) {
		/*local*/String Code = "return";
		if(Node.Expr != null) {
			Code += " " + this.VisitNode(Node.Expr);
		}
		this.PushSourceCode(Code);
		this.StopVisitor(Node);
	}

	@Override public void VisitIndexerNode(GtIndexerNode Node) {
		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.VisitNode(Node.GetAt(0)) + "]"); // FIXME: Multi
	}

	@Override public final void VisitConstructorNode(GtConstructorNode Node) {
		/*local*/int ParamSize = LibGreenTea.ListSize(Node.ParamList);
		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
		this.PushSourceCode(this.ApplyMacro(Template, Node.ParamList));
	}

	@Override public void VisitNewNode(GtNewNode Node) {
		this.PushSourceCode(this.GetNewOperator(Node.Type));
	}

	@Override public void VisitApplyNode(GtApplyNode Node) {
		/*local*/String Program = this.GenerateApplyFunc(Node);
		this.PushSourceCode(Program);
	}

	@Override public void VisitSuffixNode(GtSuffixNode Node) {
		/*local*/String FuncName = Node.Token.ParsedText;
		/*local*/String Expr = this.VisitNode(Node.Expr);
		if(LibGreenTea.EqualsString(FuncName, "++")) {
		}
		else if(LibGreenTea.EqualsString(FuncName, "--")) {
		}
		else {
			LibGreenTea.DebugP(FuncName + " is not supported suffix operator!!");
		}
		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc1(Node.Func, FuncName, true, Expr) + ")");
	}

	@Override public void VisitSelfAssignNode(GtSelfAssignNode Node) {
		/*local*/String FuncName = Node.Token.ParsedText;
		/*local*/String Left = this.VisitNode(Node.LeftNode);
		/*local*/String Right = this.VisitNode(Node.RightNode);
		this.PushSourceCode(Left + " = " + SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right));
	}

	@Override public void VisitUnaryNode(GtUnaryNode Node) {
		/*local*/String FuncName = Node.Token.ParsedText;
		/*local*/String Expr = this.VisitNode(Node.Expr);
		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc1(Node.Func, FuncName, false, Expr) + ")");
	}

	@Override public void VisitBinaryNode(GtBinaryNode Node) {
		/*local*/String FuncName = Node.Token.ParsedText;
		/*local*/String Left = this.VisitNode(Node.LeftNode);
		/*local*/String Right = this.VisitNode(Node.RightNode);
		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right) + ")");
	}

	@Override public void VisitGetterNode(GtGetterNode Node) {
		this.PushSourceCode(this.VisitNode(Node.Expr) + this.MemberAccessOperator + Node.Func.FuncName);
	}
	@Override public void VisitAssignNode(GtAssignNode Node) {
		this.PushSourceCode(this.VisitNode(Node.LeftNode) + " = " + this.VisitNode(Node.RightNode));
	}

	@Override public void VisitAndNode(GtAndNode Node) {
		/*local*/String Left = this.VisitNode(Node.LeftNode);
		/*local*/String Right = this.VisitNode(Node.RightNode);
		this.PushSourceCode("(" + Left + " " + this.LogicalAndOperator +" " + Right + ")");
	}

	@Override public void VisitOrNode(GtOrNode Node) {
		/*local*/String Left = this.VisitNode(Node.LeftNode);
		/*local*/String Right = this.VisitNode(Node.RightNode);
		this.PushSourceCode("(" + Left + " " + this.LogicalOrOperator +" " + Right + ")");
	}

	@Override public void VisitTrinaryNode(GtTrinaryNode Node) {
		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
		/*local*/String ThenExpr = this.VisitNode(Node.ThenExpr);
		/*local*/String ElseExpr = this.VisitNode(Node.ElseExpr);
		this.PushSourceCode("((" + CondExpr + ")? " + ThenExpr + " : " + ElseExpr + ")");
	}

	@Override public void VisitBreakNode(GtBreakNode Node) {
		/*local*/String Code = this.BreakKeyword;
		if(this.HasLabelSupport) {
			/*local*/String Label = Node.Label;
			if(Label != null) {
				Code += " " + Label;
			}
		}
		this.PushSourceCode(Code);
		this.StopVisitor(Node);
	}

	@Override public void VisitContinueNode(GtContinueNode Node) {
		/*local*/String Code = this.ContinueKeyword;
		if(this.HasLabelSupport) {
			/*local*/String Label = Node.Label;
			if(Label != null) {
				Code += " " + Label;
			}
		}
		this.PushSourceCode(Code);
		this.StopVisitor(Node);
	}

	@Override public void VisitSwitchNode(GtSwitchNode Node) {
		/*local*/String Code = "switch (" + this.VisitNode(Node.MatchNode) + ") {" + this.LineFeed;
		/*local*/int i = 0;
		while(i < Node.CaseList.size()) {
			/*local*/GtNode Case  = Node.CaseList.get(i);
			/*local*/GtNode Block = Node.CaseList.get(i+1);
			Code += this.GetIndentString() + "case " + this.VisitNode(Case) + ":";
			if(this.IsEmptyBlock(Block)) {
				this.Indent();
				Code += this.LineFeed + this.GetIndentString() + "/* fall-through */" + this.LineFeed;
				this.UnIndent();
			}
			else {
				Code += this.VisitBlockWithIndent(Block, true) + this.LineFeed;
			}
			i = i + 2;
		}
		if(Node.DefaultBlock != null) {
			Code += this.GetIndentString() + "default: ";
			Code += this.VisitBlockWithIndent(Node.DefaultBlock, true) + this.LineFeed;
		}
		Code += this.GetIndentString() + "}";
		this.PushSourceCode(Code);
	}

}
