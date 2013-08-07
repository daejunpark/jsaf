/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.widl

import kr.ac.kaist.jsaf.scala_src.nodes._

trait WIDLWalker {
  def walk(node: Any): Unit = node match {
    case SWModule(info, attrs, name, defs) => walk(attrs); walk(defs)
    case SWInterface(info, attrs, name, parent, members) => walk(attrs); walk(parent); walk(members)
    case SWCallback(info, attrs, name, returnType, args) => walk(attrs); walk(returnType); walk(args)
    case SWDictionary(info, attrs, name, parent, members) => walk(attrs); walk(parent); walk(members)
    case SWException(info, attrs, name, parent, members) => walk(attrs); walk(parent); walk(members)
    case SWEnum(info, attrs, name, enumValueList) => walk(attrs); walk(enumValueList)
    case SWTypedef(info, attrs, typ, name) => walk(attrs); walk(typ)
    case SWImplementsStatement(info, attrs, name, parent) => walk(attrs)
    case SWConst(info, attrs, typ, name, value) => walk(attrs); walk(typ); walk(value)
    case SWAttribute(info, attrs, typ, name, exns) => walk(attrs); walk(typ); walk(exns)
    case SWOperation(info, attrs, qualifiers, returnType, name, args, exns) => walk(attrs); walk(qualifiers); walk(returnType); walk(args); walk(exns)
    case SWDictionaryMember(info, attrs, typ, name, default) => walk(attrs); walk(typ); walk(default)
    case SWExceptionField(info, attrs, typ, name) => walk(attrs); walk(typ)
    case SWBoolean(info, value) =>
    case SWFloat(info, value) =>
    case SWInteger(info, value) =>
    case SWString(info, str) =>
    case SWNull(info) =>
    case SWAnyType(info, suffix) => walk(suffix)
    case SWNamedType(info, suffix, name) => walk(suffix)
    case SWArrayType(info, suffix, typ) => walk(suffix); walk(typ)
    case SWSequenceType(info, suffix, typ) => walk(suffix); walk(typ)
    case SWUnionType(info, suffix, types) => walk(suffix); walk(types)
    case SWArgument(info, attributes, typ, name, default) => walk(attributes); walk(typ); walk(default)
    case SWId(info, name) =>
    case SWQId(info, name) => walk(name)
    case widlList: List[_] => for(widl <- widlList) walk(widl)
    case Some(widl) => walk(widl)
    case None =>
    case _ =>
  }
}
