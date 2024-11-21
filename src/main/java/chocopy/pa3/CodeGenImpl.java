WeChat: cstutorcs
QQ: 749389476
Email: tutorcs@163.com
package chocopy.pa3;

import java.util.ArrayList;
import java.util.List;

import chocopy.pa3.RiscV.Register;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.astnodes.*;
import chocopy.common.codegen.FuncInfo;
import chocopy.common.codegen.Label;
import chocopy.common.codegen.SymbolInfo;

import static chocopy.pa3.RiscVAsmWriter.PhysicalRegister.*;

/**
 * This is where the main implementation of PA3 will live.
 *
 * A large part of the functionality has already been implemented
 * in the base class, CodeGenBase. Make sure to read through that
 * class, since you will want to use many of its fields
 * and utility methods in this class when emitting code.
 *
 * Also read the PDF spec for details on what the base class does and
 * what APIs it exposes for its sub-class (this one). Of particular
 * importance is knowing what all the SymbolInfo classes contain.
 */
public class CodeGenImpl extends CodeGenBase {

    /** A code generator emitting instructions to BACKEND. */
    public CodeGenImpl(RiscVAsmWriter asmWriter) {
        super(asmWriter);
    }

    /** Operation on None. */
    private final Label errorNone = new Label("error.None");
    /** Division by zero. */
    private final Label errorDiv = new Label("error.Div");
    /** Index out of bounds. */
    private final Label errorOob = new Label("error.OOB");

    /**
     * Emits the top level of the program.
     *
     * This method is invoked exactly once, and is surrounded
     * by some boilerplate code that: (1) initializes the heap
     * before the top-level begins and (2) exits after the top-level
     * ends.
     *
     * You only need to generate code for statements.
     *
     * @param statements top level statements
     */
    protected void emitTopLevel(List<Stmt> statements) {
        StmtsToRiscV stmtsToRiscV = new StmtsToRiscV(null);
        asmWriter.emitADDI(SP, SP, -2 * asmWriter.getWordSize(),
                         "Saved FP and saved RA (unused at top level).");
        asmWriter.emitSW(ZERO, SP, 0, "Top saved FP is 0.");
        asmWriter.emitSW(ZERO, SP, 4, "Top saved RA is 0.");
        asmWriter.emitADDI(FP, SP, 2 * asmWriter.getWordSize(),
                         "Set FP to previous SP.");

        for (Stmt stmt : statements) {
            stmtsToRiscV.dispatchStmt(stmt);
        }

        asmWriter.emitLI(A0, EXIT_ECALL, "Code for ecall: exit");
        asmWriter.emitEcall(null);
    }

    /**
     * Emits the code for a function described by FUNCINFO.
     *
     * This method is invoked once per function and method definition.
     * At the code generation stage, nested functions are emitted as
     * separate functions of their own. So if function `bar` is nested within
     * function `foo`, you only emit `foo`'s code for `foo` and only emit
     * `bar`'s code for `bar`.
     */
    protected void emitUserDefinedFunction(FuncInfo funcInfo) {
        asmWriter.emitGlobalLabel(funcInfo.getCodeLabel());
        StmtsToRiscV stmtsToRiscV = new StmtsToRiscV(funcInfo);

        for (Stmt stmt : funcInfo.getStatements()) {
            stmtsToRiscV.dispatchStmt(stmt);
        }

        asmWriter.emitMV(A0, ZERO, "Returning None implicitly");
        asmWriter.emitLocalLabel(stmtsToRiscV.epilogue, "Epilogue");

        // FIXME: {... reset fp etc. ...}
        asmWriter.emitJR(RA, "Return to caller");
    }

    /** An analyzer that encapsulates code generation for statements. */
    private class StmtsToRiscV extends AstVisitor {
        /*
         * The symbol table has all the info you need to determine
         * what a given identifier 'x' in the current scope is. You can
         * use it as follows:
         *   SymbolInfo x = sym.get("x");
         *
         * A SymbolInfo can be one the following:
         * - ClassInfo: a descriptor for classes
         * - FuncInfo: a descriptor for functions/methods
         * - AttrInfo: a descriptor for attributes
         * - GlobalVarInfo: a descriptor for global variables
         * - StackVarInfo: a descriptor for variables allocated on the stack,
         *      such as locals and parameters
         *
         * Since the input program is assumed to be semantically
         * valid and well-typed at this stage, you can always assume that
         * the symbol table contains valid information. For example, in
         * an expression `foo()` you KNOW that sym.get("foo") will either be
         * a FuncInfo or ClassInfo, but not any of the other infos
         * and never null.
         *
         * The symbol table in funcInfo has already been populated in
         * the base class: CodeGenBase. You do not need to add anything to
         * the symbol table. Simply query it with an identifier name to
         * get a descriptor for a function, class, variable, etc.
         *
         * The symbol table also maps nonlocal and global vars, so you
         * only need to lookup one symbol table and it will fetch the
         * appropriate info for the var that is currently in scope.
         */

        /** Symbol table for my statements. */
        private SymbolTable<SymbolInfo> sym;

        /** Label of code that exits from procedure. */
        protected Label epilogue;

        /** The descriptor for the current function, or null at the top
         *  level. */
        private FuncInfo funcInfo;

        /** An analyzer for the function described by FUNCINFO0, which is null
         *  for the top level. */
        StmtsToRiscV(FuncInfo funcInfo0) {
            funcInfo = funcInfo0;
            if (funcInfo == null) {
                sym = globalSymbols;
            } else {
                sym = funcInfo.getSymbolTable();
            }
            epilogue = generateLocalLabel();
        }

        // FIXME: Example of statement.
        @Override
        public void visit(ReturnStmt stmt) {
            // FIXME: Here, we emit an instruction that does nothing. Clearly,
            // this is wrong, and you'll have to fix it.
            // This is here just to demonstrate how to emit a
            // RISC-V instruction.
            asmWriter.emitMV(ZERO, ZERO, "No-op");
        }

        // FIXME: More, of course.

    }

    /*
     * If you want a challenge, or you want a better foundation on which to
     * participate in the optimization leaderboard, instead of implementing the
     * above class (which emits assembly instructions directly), you can instead
     * implement the following class, which emits RISC-V instructions into an
     * in-memory representation which you can further optimize.
     *
     * Implementing this class is optional; it is perfectly possible (and
     * recommended) to complete PA3 with full score without using this class.
     *
     * Note that the skeleton for this way was newly implemented this semester,
     * and has not been tested with a end-to-end implementation of ChocoPy.
     * **There may be (and likely are) bugs in the provided skeleton.**
     * TA support for this way of implementation will also be limited.
     */
    private class StmtsToOptRiscV {
        /** Symbol table for my statements. */
        private SymbolTable<SymbolInfo> sym;

        /** Label of code that exits from procedure. */
        protected Label epilogue;

        /** The descriptor for the current function, or null at the top
         *  level. */
        private FuncInfo funcInfo;

        private List<RiscV.Instr> instrs = new ArrayList<>();
        private int nextRegisterIdx = 0;
        private RiscVInstrFactory fact = new RiscVInstrFactory();

        /** An analyzer for the function described by FUNCINFO0, which is null
         *  for the top level. */
        StmtsToOptRiscV(FuncInfo funcInfo0) {
            funcInfo = funcInfo0;
            if (funcInfo == null) {
                sym = globalSymbols;
            } else {
                sym = funcInfo.getSymbolTable();
            }
            epilogue = generateLocalLabel();
        }

        private Register freshRegister() {
            // You can use this method to get a fresh physical register instead, but if you
            // take that route, you will have to keep track of which registers are in use at
            // a given time (and have a strategy for spilling in case you run out of
            // physical registers).
            return new RiscV.VirtualRegister("t" + nextRegisterIdx++);
        }

        public List<RiscV.Instr> getInstrs() {
            // The method RiscVAsmWriter.emitRiscVInstr can emit a single
            // instruction to the output stream. Note that all virtual registers
            // must be assigned to physical registers before emission is possible.
            return instrs;
        }

        public void dispatchStmt(Stmt stmt) {
            switch (stmt) {
                case ExprStmt exprStmt -> visit(exprStmt);
                default ->
                    throw new UnsupportedOperationException(
                        "dispatchStmt not implemented for " +
                        stmt.getClass().getCanonicalName());
            }
        }

        public void visit(ExprStmt stmt) {
            dispatchExpr(stmt.expr);
        }

        public Register dispatchExpr(Expr expr) {
            return switch (expr) {
                case BinaryExpr binaryExpr -> visit(binaryExpr);
                default ->
                    throw new UnsupportedOperationException(
                        "dispatchExpr not implemented for " +
                        expr.getClass().getCanonicalName());
            };
        }

        public Register visit(BinaryExpr expr) {
            var left = dispatchExpr(expr.left);
            var right = dispatchExpr(expr.right);
            var dest = freshRegister();
            switch (expr.operator) {
                case "+" -> {
                    var instr = fact.add(dest, left, right);
                    instrs.add(instr);
                }
                default ->
                    throw new UnsupportedOperationException(
                        "visit(BinaryExpr) not implemented for " + expr.operator);
            }
            return dest;
        }
    }

    /**
     * Emits custom code in the CODE segment.
     *
     * This method is called after emitting the top level and the
     * function bodies for each function.
     *
     * You can use this method to emit anything you want outside of the
     * top level or functions, e.g. custom routines that you may want to
     * call from within your code to do common tasks. This is not strictly
     * needed. You might not modify this at all and still complete
     * the assignment.
     *
     * To start you off, here is an implementation of three routines that
     * will be commonly needed from within the code you will generate
     * for statements.
     *
     * The routines are error handlers for operations on None, index out
     * of bounds, and division by zero. They never return to their caller.
     * Just jump to one of these routines to throw an error and
     * exit the program. For example, to throw an OOB error:
     *   asmWriter.emitJ(errorOob, "Go to out-of-bounds error and abort");
     *
     */
    protected void emitCustomCode() {
        emitErrorFunc(errorNone, "Operation on None");
        emitErrorFunc(errorDiv, "Division by zero");
        emitErrorFunc(errorOob, "Index out of bounds");
    }

    /** Emit an error routine labeled ERRLABEL that aborts with message MSG. */
    private void emitErrorFunc(Label errLabel, String msg) {
        asmWriter.emitGlobalLabel(errLabel);
        asmWriter.emitLI(A0, ERROR_NONE, "Exit code for: " + msg);
        asmWriter.emitLA(A1, constants.getStrConstant(msg),
                       "Load error message as str");
        asmWriter.emitADDI(A1, A1, getAttrOffset(strClass, "__str__"),
                         "Load address of attribute __str__");
        asmWriter.emitJ(abortLabel, "Abort");
    }
}
