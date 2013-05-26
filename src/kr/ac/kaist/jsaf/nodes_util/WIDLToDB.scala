/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.nodes_util

import _root_.java.io.BufferedWriter
import _root_.java.io.FileWriter
import _root_.java.util.ArrayList
import _root_.java.util.{List => JList}

import scala.io.Source
import edu.rice.cs.plt.iter.IterUtil
import edu.rice.cs.plt.tuple.Option
import kr.ac.kaist.jsaf.nodes._
import kr.ac.kaist.jsaf.nodes_util.{ NodeUtil => NU }
import kr.ac.kaist.jsaf.nodes_util.{ WIDLFactory => WF }
import kr.ac.kaist.jsaf.nodes_util.{ WIDLToString => WS }
import kr.ac.kaist.jsaf.scala_src.nodes._
import kr.ac.kaist.jsaf.scala_src.useful.Lists._
import kr.ac.kaist.jsaf.scala_src.useful.Options._
import kr.ac.kaist.jsaf.scala_src.useful.Sets._

object WIDLToDB extends Walker {

  object Done extends Exception

  //////////////////////////////////////////////////////////
  // WIDL nodes enumeration
  //////////////////////////////////////////////////////////
  // WDefinition
  private val MODULE = 56
  private val INTERFACE = 0
  private val CALLBACK = 1
  private val DICTIONARY = 2
  private val EXCEPTION = 3
  private val ENUM = 4
  private val TYPEDEF = 5
  private val IMPLEMENTS = 6
  // WTypeSuffix
  private val TSARRAY = 7
  private val TSQUESTION = 8
  // WEAttribute
  private val EAARRAY = 55
  private val EASTRING = 9
  private val EAQUESTION = 10
  private val EAELLIPSIS = 11
  private val EAOPTIONAL = 12
  private val EAATTRIBUTE = 13
  private val EACALLBACK = 14
  private val EACONST = 15
  private val EACREATOR = 16
  private val EADELETER = 17
  private val EADICTIONARY = 18
  private val EAENUM = 19
  private val EAEXCEPTION = 20
  private val EAGETTER = 21
  private val EAIMPLEMENTS = 22
  private val EAINHERIT = 23
  private val EAINTERFACE = 24
  private val EAREADONLY = 25
  private val EALEGACYCALLER = 26
  private val EAPARTIAL = 27
  private val EASETTER = 28
  private val EASTATIC = 29
  private val EASTRINGIFIER = 30
  private val EATYPEDEF = 31
  private val EAUNRESTRICTED = 32
  // WQualifier
  private val QSTATIC = 33
  private val QGETTER = 34
  private val QSETTER = 35
  private val QCREATOR = 36
  private val QDELETER = 37
  private val QLEGACYCALLER = 38
  // WLiteral
  private val BOOLEAN = 39
  private val FLOAT = 40
  private val INTEGER = 41
  private val STRING = 42
  private val NULL = 43
  // WType
  private val ANYTYPE = 44
  private val NAMEDTYPE = 45
  private val ARRAYTYPE = 46
  private val SEQUENCETYPE = 47
  private val UNIONTYPE = 48
  // Boolean
  private val TRUE = 49
  private val FALSE = 50
  // Member
  private val CONST = 51
  private val ATTRIBUTE = 52
  private val OPERATION = 53
  private val EXCEPTIONFIELD = 54
  private val QID = 57
  
  // maximum number = 57

  var bw : BufferedWriter = null
  val span = WF.makeSpan("from WIDL DB")

  def join(all: List[Any]): Unit = all.distinct.foreach(walkUnit)

  //////////////////////////////////////////////////////////
  // store WIDL to DB
  //////////////////////////////////////////////////////////
  /**
   * Store WIDL to db
   */
  def storeToDB(fileName: String, program: JList[WDefinition]) = {
    val fw = new FileWriter(fileName+".db")
    bw = new BufferedWriter(fw)
    toList(program).foreach(walkUnit)
    bw.close
    fw.close
  }

  override def walkUnit(node:Any): Unit = node match {
    case SWModule(info, _, name, defs) =>
      bw.write(MODULE+"\n"+name+"\n")
      bw.write(defs.length+"\n"); join(defs)
    case SWCallback(info, attrs, name, returnType, args) =>
      bw.write(CALLBACK+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs); walkUnit(returnType)
      bw.write(args.length+"\n"); join(args)
    case SWInterface(info, attrs, name, parent, members) =>
      bw.write(INTERFACE+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs)
      if (parent.isSome) bw.write(parent.get.getName)
      bw.write("\n"+members.length+"\n")
      join(members)
    case SWDictionary(info, attrs, name, parent, members) =>
      bw.write(DICTIONARY+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs)
      if (parent.isSome) bw.write(parent.get.getName)
      bw.write("\n"+members.length+"\n")
      join(members)
    case SWDictionaryMember(info, attrs, typ, name, default) =>
      bw.write(attrs.length+"\n")
      join(attrs)
      walkUnit(typ); bw.write(name+"\n")
      if (default.isSome) walkUnit(default.get) else bw.write("\n")
    case SWException(info, attrs, name, parent, members) =>
      bw.write(EXCEPTION+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs)
      if (parent.isSome) bw.write(parent.get.getName)
      bw.write("\n"+members.length+"\n")
      join(members)
    case SWEnum(info, attrs, name, members) =>
      bw.write(ENUM+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs)
      bw.write(members.length+"\n")
      join(members)
    case SWTypedef(info, attrs, typ, name) =>
      bw.write(TYPEDEF+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs); walkUnit(typ)
    case SWImplementsStatement(info, attrs, name, parent) =>
      bw.write(IMPLEMENTS+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs); bw.write(parent+"\n")
    case SWConst(info, attrs, typ, name, value) =>
      bw.write(CONST+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs); walkUnit(typ); walkUnit(value)
    case SWBoolean(info, value) =>
      bw.write(BOOLEAN+"\n")
      if(value) bw.write(TRUE+"\n") else bw.write(FALSE+"\n")
    case SWFloat(info, value) =>
      bw.write(FLOAT+"\n"+value+"\n")
    case SWInteger(info, value) =>
      bw.write(INTEGER+"\n"+value+"\n")
    case SWString(info, str) =>
      bw.write(STRING+"\n"+str+"\n")
    case SWNull(info) =>
      bw.write(NULL+"\n")
    case SWAttribute(info, attrs, typ, name) =>
      bw.write(ATTRIBUTE+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs); walkUnit(typ)
    case SWExceptionField(info, attrs, typ, name) =>
      bw.write(EXCEPTIONFIELD+"\n"+name+"\n"+attrs.length+"\n")
      join(attrs); walkUnit(typ)
    case SWOperation(info, attrs, qualifiers, returnType, name, args, exns) =>
      bw.write(OPERATION+"\n")
      if (name.isSome) bw.write(name.get)
      bw.write("\n"+attrs.length+"\n"); join(attrs)
      bw.write(qualifiers.length+"\n"); join(qualifiers)
      walkUnit(returnType)
      bw.write(args.length+"\n"); join(args)
      bw.write(exns.length+"\n"); join(exns)
    case SWArgument(info, attrs, typ, name, default) =>
      bw.write(attrs.length+"\n"); join(attrs)
      walkUnit(typ); bw.write(name+"\n")
      if (default.isSome) walkUnit(default.get) else bw.write("\n")
    case SWAnyType(info, suffix) =>
      bw.write(ANYTYPE+"\n"+suffix.length+"\n")
      join(suffix)
    case SWUnionType(info, suffix, types) =>
      bw.write(UNIONTYPE+"\n"+suffix.length+"\n")
      join(suffix)
      bw.write(types.length+"\n")
      join(types)
    case SWArrayType(info, suffix, typ) =>
      bw.write(ARRAYTYPE+"\n"+suffix.length+"\n")
      join(suffix)
      walkUnit(typ)
    case SWNamedType(info, suffix, name) =>
      bw.write(NAMEDTYPE+"\n"+suffix.length+"\n")
      join(suffix)
      bw.write(name+"\n")
    case SWSequenceType(info, suffix, typ) =>
      bw.write(SEQUENCETYPE+"\n"+suffix.length+"\n")
      join(suffix)
      walkUnit(typ)
    case SWQId(info, exns) =>
      bw.write(QID+"\n"+exns.length+"\n")
      join(exns)
    case SWSpanInfo(span) =>
    case _ => walkUnitJavaNode(node)
  }

  def walkUnitJavaNode(node:Any): Unit =
    if (node.isInstanceOf[WTSArray]) bw.write(TSARRAY+"\n")
    else if (node.isInstanceOf[WTSQuestion]) bw.write(TSQUESTION+"\n")
    else if (node.isInstanceOf[WEAArray]) bw.write(EAARRAY+"\n")
    else if (node.isInstanceOf[WEAString]) {
      bw.write(EASTRING+"\n")
      bw.write(node.asInstanceOf[WEAString].getStr+"\n")
    } else if (node.isInstanceOf[WEAQuestion]) bw.write(EAQUESTION+"\n")
    else if (node.isInstanceOf[WEAEllipsis]) bw.write(EAELLIPSIS+"\n")
    else if (node.isInstanceOf[WEAOptional]) bw.write(EAOPTIONAL+"\n")
    else if (node.isInstanceOf[WEAAttribute]) bw.write(EAATTRIBUTE+"\n")
    else if (node.isInstanceOf[WEACallback]) bw.write(EACALLBACK+"\n")
    else if (node.isInstanceOf[WEAConst]) bw.write(EACONST+"\n")
    else if (node.isInstanceOf[WEACreator]) bw.write(EACREATOR+"\n")
    else if (node.isInstanceOf[WEADeleter]) bw.write(EADELETER+"\n")
    else if (node.isInstanceOf[WEADictionary]) bw.write(EADICTIONARY+"\n")
    else if (node.isInstanceOf[WEAEnum]) bw.write(EAENUM+"\n")
    else if (node.isInstanceOf[WEAException]) bw.write(EAEXCEPTION+"\n")
    else if (node.isInstanceOf[WEAGetter]) bw.write(EAGETTER+"\n")
    else if (node.isInstanceOf[WEAImplements]) bw.write(EAIMPLEMENTS+"\n")
    else if (node.isInstanceOf[WEAInherit]) bw.write(EAINHERIT+"\n")
    else if (node.isInstanceOf[WEAInterface]) bw.write(EAINTERFACE+"\n")
    else if (node.isInstanceOf[WEAReadonly]) bw.write(EAREADONLY+"\n")
    else if (node.isInstanceOf[WEALegacycaller]) bw.write(EALEGACYCALLER+"\n")
    else if (node.isInstanceOf[WEAPartial]) bw.write(EAPARTIAL+"\n")
    else if (node.isInstanceOf[WEASetter]) bw.write(EASETTER+"\n")
    else if (node.isInstanceOf[WEAStatic]) bw.write(EASTATIC+"\n")
    else if (node.isInstanceOf[WEAStringifier]) bw.write(EASTRINGIFIER+"\n")
    else if (node.isInstanceOf[WEATypedef]) bw.write(EATYPEDEF+"\n")
    else if (node.isInstanceOf[WEAUnrestricted]) bw.write(EAUNRESTRICTED+"\n")
    else if (node.isInstanceOf[WQStatic]) bw.write(QSTATIC+"\n")
    else if (node.isInstanceOf[WQGetter]) bw.write(QGETTER+"\n")
    else if (node.isInstanceOf[WQSetter]) bw.write(QSETTER+"\n")
    else if (node.isInstanceOf[WQCreator]) bw.write(QCREATOR+"\n")
    else if (node.isInstanceOf[WQDeleter]) bw.write(QDELETER+"\n")
    else if (node.isInstanceOf[WQLegacycaller]) bw.write(QLEGACYCALLER+"\n")
    else bw.write("#@#"+node.getClass.toString+"\n")

  ////////////////////////////////////////////////////////
  // read the DB to reconstruct WIDL
  ////////////////////////////////////////////////////////
  /**
   * Read the DB to reconstruct WIDL
   */
  def readDB(fileName: String): Unit = {
    val bs = Source.fromFile(fileName+".db")
    val br = bs.getLines
    val tfw = new FileWriter(fileName+".test")
    val tbw = new BufferedWriter(tfw)
    var result = List[WDefinition]()
    def readLine() = if (br.hasNext) br.next else throw Done
    def readInt() = Integer.parseInt(readLine)
    def readDefinition(): WDefinition = readInt match {
      case MODULE => readModule
      case INTERFACE => readInterface
      case CALLBACK => readCallback
      case DICTIONARY => readDictionary
      case EXCEPTION => readException
      case ENUM => readEnum
      case TYPEDEF => readTypedef
      case IMPLEMENTS => readImplements
    }
    def readModule(): WModule = {
      val name = readLine
      val defsNum = readInt
      var defs = new ArrayList[WDefinition](defsNum)
      for (i <- 0 until defsNum) defs.add(readDefinition)
      WF.mkModule(span, name, defs)
    }
    def readInterface(): WInterface = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val parentOpt = readLine
      val parent = if (parentOpt.isEmpty) none else some(WF.mkId(span, parentOpt))
      val memsNum = readInt
      var mems = new ArrayList[WInterfaceMember](memsNum)
      for (i <- 0 until memsNum) mems.add(readInterfaceMember)
      val dict = WF.mkInterface(span, name, parent, mems)
      WF.addAttrs(attrs, dict).asInstanceOf[WInterface]
    }
    def readInterfaceMember(): WInterfaceMember = readInt match {
      case CONST => readConst(INTERFACE)
      case ATTRIBUTE => readWAttribute
      case OPERATION => readOperation
      case _ => throw new IllegalArgumentException("wrong InterfaceMember")
    }
    def readCallback(): WCallback = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      val argsNum = readInt
      var args = new ArrayList[WArgument](argsNum)
      for (i <- 0 until argsNum) args.add(readArgument)
      val call = WF.mkCallback(span, name, typ, args)
      WF.addAttrs(attrs, call).asInstanceOf[WCallback]
    }
    def readDictionary(): WDictionary = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val parentOpt = readLine
      val parent = if (parentOpt.isEmpty) none else some(WF.mkId(span, parentOpt))
      val memsNum = readInt
      var mems = new ArrayList[WDictionaryMember](memsNum)
      for (i <- 0 until memsNum) mems.add(readDictionaryMember)
      val dict = WF.mkDictionary(span, name, parent, mems)
      WF.addAttrs(attrs, dict).asInstanceOf[WDictionary]
    }
    def readDictionaryMember(): WDictionaryMember = {
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      val name = readLine
      val defaultOpt = readLine
      val default = if (defaultOpt.isEmpty) none else some(readLiteral(defaultOpt))
      WF.addAttrs(attrs, WF.mkDictionaryMember(span, typ, name, default))
    }
    def readException(): WException = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val parentOpt = readLine
      val parent = if (parentOpt.isEmpty) none else some(WF.mkId(span, parentOpt))
      val memsNum = readInt
      var mems = new ArrayList[WExceptionMember](memsNum)
      for (i <- 0 until memsNum) mems.add(readExceptionMember)
      val dict = WF.mkException(span, name, parent, mems)
      WF.addAttrs(attrs, dict).asInstanceOf[WException]
    }
    def readExceptionMember(): WExceptionMember = readInt match {
      case CONST => readConst(EXCEPTION)
      case EXCEPTIONFIELD => readExceptionField
      case _ => throw new IllegalArgumentException("wrong ExceptionMember")
    }
    def readConst(parent: Int): WConst = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      val value = readLiteral(readLine)
      val const = WF.mkConst(span, typ, name, value)
      parent match {
        case INTERFACE =>
          WF.addAttrs(attrs, const.asInstanceOf[WInterfaceMember]).asInstanceOf[WConst]
        case EXCEPTION =>
          WF.addAttrs(attrs, const.asInstanceOf[WExceptionMember]).asInstanceOf[WConst]
      }
    }
    def readWAttribute(): WAttribute = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      WF.addAttrs(attrs, WF.mkAttribute(span, attrs, typ, name)).asInstanceOf[WAttribute]
    }
    def readOperation(): WOperation = {
      val nameOpt = readLine
      val name = if (nameOpt.isEmpty) none else some(nameOpt)
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val qualsNum = readInt
      var quals = new ArrayList[WQualifier](qualsNum)
      for (i <- 0 until qualsNum) quals.add(readQualifier)
      val typ = readType
      val argsNum = readInt
      var args = new ArrayList[WArgument](argsNum)
      for (i <- 0 until argsNum) args.add(readArgument)
      val exnsNum = readInt
      var exns = new ArrayList[WQId](exnsNum)
      for (i <- 0 until exnsNum) exns.add(readExn)
      WF.mkOperation(span, attrs, quals, typ, name, args, exns)
    }
    def readArgument(): WArgument = {
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      val name = readLine
      val defaultOpt = readLine
      val default = if (defaultOpt.isEmpty) none else some(readLiteral(defaultOpt))
      WF.mkArgument(span, attrs, typ, name, default)
    }
    def readExceptionField(): WExceptionField = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      WF.addAttrs(attrs, WF.mkExceptionField(span, typ, name)).asInstanceOf[WExceptionField]
    }
    def readEnum(): WEnum = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val memsNum = readInt
      var mems = new ArrayList[WString](memsNum)
      for (i <- 0 until memsNum) mems.add(readString)
      val enum = WF.mkEnum(span, name, mems)
      WF.addAttrs(attrs, enum).asInstanceOf[WEnum]
    }
    def readTypedef(): WTypedef = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val typ = readType
      WF.mkTypedef(span, attrs, typ, name)
    }
    def readImplements(): WImplementsStatement = {
      val name = readLine
      val attrsNum = readInt
      var attrs = new ArrayList[WEAttribute](attrsNum)
      for (i <- 0 until attrsNum) attrs.add(readAttribute)
      val parent = readLine
      val imp = WF.mkImplementsStatement(span, name, parent)
      WF.addAttrs(attrs, imp).asInstanceOf[WImplementsStatement]
    }
    def readExn(): WQId = {
      readLine;
      val namesNum = readInt
      var names = new ArrayList[String](namesNum)
      for (i <- 0 until namesNum) names.add(readLine)
      WF.mkQId(span, names)
    }
    def readString(): WString = { readLine; WF.mkString(span, readLine) }
    def readAttribute(): WEAttribute = readInt match {
      case EAARRAY => WF.eaArray
      case EASTRING => WF.mkEAString(readLine)
      case EAQUESTION => WF.eaQuestion
      case EAELLIPSIS => WF.eaEllipsis
      case EAOPTIONAL => WF.eaOptional
      case EAATTRIBUTE => WF.eaAttribute
      case EACALLBACK => WF.eaCallback
      case EACONST => WF.eaConst
      case EACREATOR => WF.eaCreator
      case EADELETER => WF.eaDeleter
      case EADICTIONARY => WF.eaDictionary
      case EAENUM => WF.eaEnum
      case EAEXCEPTION => WF.eaException
      case EAGETTER => WF.eaGetter
      case EAIMPLEMENTS => WF.eaImplements
      case EAINHERIT => WF.eaInherit
      case EAINTERFACE => WF.eaInterface
      case EAREADONLY => WF.eaReadonly
      case EALEGACYCALLER => WF.eaLegacycaller
      case EAPARTIAL => WF.eaPartial
      case EASETTER => WF.eaSetter
      case EASTATIC => WF.eaStatic
      case EASTRINGIFIER => WF.eaStringifier
      case EATYPEDEF => WF.eaTypedef
      case EAUNRESTRICTED => WF.eaUnrestricted
      case _ => throw new IllegalArgumentException("wrong EAttribute")
    }
    def readTypeSuffix(): WTypeSuffix = readInt match {
      case TSARRAY => WF.tsArray
      case TSQUESTION => WF.tsQuestion
      case _ => throw new IllegalArgumentException("wrong TypeSuffix")
    }
    def readQualifier(): WQualifier = readInt match {
      case QSTATIC => WF.qStatic
      case QGETTER => WF.qGetter
      case QSETTER => WF.qSetter
      case QCREATOR => WF.qCreator
      case QDELETER => WF.qDeleter
      case QLEGACYCALLER => WF.qLegacycaller
      case _ => throw new IllegalArgumentException("wrong Qualifier")
    }
    def readType(): WType = readInt match {
      case ANYTYPE =>
        val suffNum = readInt
        var suff = new ArrayList[WTypeSuffix](suffNum)
        for (i <- 0 until suffNum) suff.add(readTypeSuffix)
        WF.mkAnyType(span, suff)
      case NAMEDTYPE =>
        val suffNum = readInt
        var suff = new ArrayList[WTypeSuffix](suffNum)
        for (i <- 0 until suffNum) suff.add(readTypeSuffix)
        WF.mkNamedType(span, readLine, suff)
      case ARRAYTYPE =>
        val suffNum = readInt
        var suff = new ArrayList[WTypeSuffix](suffNum)
        for (i <- 0 until suffNum) suff.add(readTypeSuffix)
        val typ = readType
        WF.mkArrayType(span, typ, suff)
      case SEQUENCETYPE =>
        val suffNum = readInt
        var suff = new ArrayList[WTypeSuffix](suffNum)
        for (i <- 0 until suffNum) suff.add(readTypeSuffix)
        val typ = readType
        WF.mkSequenceType(span, typ, suff)
      case UNIONTYPE =>
        val suffNum = readInt
        var suff = new ArrayList[WTypeSuffix](suffNum)
        for (i <- 0 until suffNum) suff.add(readTypeSuffix)
        val typesNum = readInt
        var types = new ArrayList[WType](typesNum)
        for (i <- 0 until typesNum) types.add(readType)
        WF.mkUnionType(span, types, suff)
      case n => throw new IllegalArgumentException("wrong Type")
    }
    def readLiteral(lit: String): WLiteral = Integer.parseInt(lit) match {
      case BOOLEAN => readInt match {
        case TRUE => WF.trueL
        case FALSE => WF.falseL
      }
      case FLOAT => WF.mkFloat(span, readLine)
      case INTEGER => WF.mkInteger(span, readLine)
      case STRING => WF.mkString(span, readLine)
      case NULL => WF.nullL
      case _ => throw new IllegalArgumentException("wrong Literal")
    }
    try {
      while (true) result ++= List(readDefinition)
    } catch {
      case Done =>
      case e => throw e
    } finally {
      result.foreach(d => tbw.write(WS.walk(d)+"\n"))
      tbw.close
      tfw.close
      bs.close
    }
  }
}
