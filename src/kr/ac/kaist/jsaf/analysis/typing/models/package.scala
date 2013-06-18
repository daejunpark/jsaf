/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.typing.domain.{LPSet, Context, Heap}
import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}

package object models {
  type SemanticFun = (Semantics, Heap, Context, Heap, Context, ControlPoint, CFG, String, CFGExpr) => ((Heap, Context),(Heap, Context))
  type AccessFun = (Heap, Context, CFG, String, CFGExpr) => (LPSet)
}
