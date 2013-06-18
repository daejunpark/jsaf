/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T, _}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import java.lang.InternalError
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsBuiltinFunc
import kr.ac.kaist.jsaf.analysis.typing.models.builtin.{BuiltinDate, BuiltinArray}

object TIZENcontactObj extends Tizen {
  private val name = "contact"
  /* predefined locations */
  val loc_obj = TIZENtizen.loc_contact
  val loc_proto = newPredefLoc(name + "Proto")

  val loc_contname: Loc        = newPreDefLoc("ContactName", Old)
  val loc_contaddr: Loc        = newPreDefLoc("ContactAddress", Old)
  val loc_contaddrarr: Loc        = newPreDefLoc("ContactAddressArr", Old)
  val loc_contphonenum: Loc        = newPreDefLoc("ContactPhoneNumber", Old)
  val loc_contphonenumarr: Loc        = newPreDefLoc("ContactPhoneNumberArr", Old)
  val loc_contemailaddr: Loc        = newPreDefLoc("ContactEmailAddress", Old)
  val loc_contemailaddrarr: Loc        = newPreDefLoc("ContactEmailAddressArr", Old)
  val loc_contanniv: Loc        = newPreDefLoc("ContactAnniversary", Old)
  val loc_contannivarr: Loc        = newPreDefLoc("ContactAnniversaryArr", Old)
  val loc_contorgan: Loc        = newPreDefLoc("ContactOrganization", Old)
  val loc_contorganarr: Loc        = newPreDefLoc("ContactOrganizationArr", Old)
  val loc_contweb: Loc        = newPreDefLoc("ContactWebSite", Old)
  val loc_contwebarr: Loc        = newPreDefLoc("ContactWebSiteArr", Old)
  val loc_types: Loc              = newPreDefLoc("types", Old)
  val loc_date: Loc               = newPreDefLoc("ContactDate", Old)

  /* constructor or object*/
  private val prop_obj: List[(String, AbsProperty)] = List(
    ("@class",        AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",        AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("@extensible",   AbsConstValue(PropValue(T))),
    ("@scope",        AbsConstValue(PropValue(Value(NullTop)))),
    ("@hasinstance",  AbsConstValue(PropValue(Value(NullTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",                  AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto",                  AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",             AbsConstValue(PropValue(T))),
    ("getAddressBooks",         AbsBuiltinFunc("tizen.contactObj.getAddressBooks", 2)),
    ("getUnifiedAddressBook",   AbsBuiltinFunc("tizen.contactObj.getUnifiedAddressBook", 0)),
    ("getDefaultAddressBook",   AbsBuiltinFunc("tizen.contactObj.getDefaultAddressBook", 0)),
    ("getAddressBook",          AbsBuiltinFunc("tizen.contactObj.getAddressBook", 1)),
    ("get",                     AbsBuiltinFunc("tizen.contactObj.get", 1)),
    ("update",                  AbsBuiltinFunc("tizen.contactObj.update", 1)),
    ("updateBatch",             AbsBuiltinFunc("tizen.contactObj.updateBatch", 3)),
    ("remove",                  AbsBuiltinFunc("tizen.contactObj.remove", 1)),
    ("removeBatch",             AbsBuiltinFunc("tizen.contactObj.removeBatch", 3)),
    ("find",                    AbsBuiltinFunc("tizen.contactObj.find", 4)),
    ("addChangeListener",       AbsBuiltinFunc("tizen.contactObj.addChangeListener", 1)),
    ("removeChangeListener",    AbsBuiltinFunc("tizen.contactObj.removeChangeListener", 1))
  )

  private val prop_types_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T))))
  )

  private val prop_contname_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactName.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("prefix", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("suffix", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("firstName", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("middleName", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("lastName", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("nicknames", AbsConstValue(PropValue(ObjectValue(Value(UndefTop), T, T, T)))),
    ("phoneticFirstName", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("phoneticLastName", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("displayName", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))))
  )

  private val prop_contaddr_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactName.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("country", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("region", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("city", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("streetAddress", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("additionalInformation", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("postalCode", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T)))),
    ("isDefault", AbsConstValue(PropValue(ObjectValue(Value(BoolTop), T, T, T)))),
    ("types", AbsConstValue(PropValue(ObjectValue(Value(loc_types), T, T, T))))
  )
  private val prop_contaddrarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_contaddr), T, T, T))))
  )
  private val prop_contphonenum_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactPhoneNumber.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("number", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("isDefault", AbsConstValue(PropValue(ObjectValue(Value(BoolTop), T, T, T)))),
    ("types", AbsConstValue(PropValue(ObjectValue(Value(loc_types), T, T, T))))
  )
  private val prop_contphonenumarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_contphonenum), T, T, T))))
  )
  private val prop_contemailaddr_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactEmailAddress.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("email", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("isDefault", AbsConstValue(PropValue(ObjectValue(Value(BoolTop), T, T, T)))),
    ("types", AbsConstValue(PropValue(ObjectValue(Value(loc_types), T, T, T))))
  )
  private val prop_contemailaddrarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_contemailaddr), T, T, T))))
  )
  private val prop_contanniv_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactAnniversary.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("date", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("label", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))))
  )
  private val prop_contannivarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_contanniv), T, T, T))))
  )
  private val prop_contorgan_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactOrganization.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("date", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("label", AbsConstValue(PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))))
  )
  private val prop_contorganarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_contorgan), T, T, T))))
  )
  private val prop_contweb_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(TIZENContactWebSite.loc_proto, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T))),
    ("url", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T)))),
    ("type", AbsConstValue(PropValue(ObjectValue(Value(StrTop), T, T, T))))
  )
  private val prop_contwebarr_ins: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Array")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(BuiltinArray.ProtoLoc, F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T))),
    ("length", AbsConstValue(PropValue(ObjectValue(UInt, T, F, F)))),
    ("@default_number", AbsConstValue(PropValue(ObjectValue(Value(loc_contweb), T, T, T))))
  )
  private val prop_date_ins: List[(String, AbsProperty)] = List(
    ("@class",               AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto",               AbsConstValue(PropValue(ObjectValue(BuiltinDate.ProtoLoc, F, F, F)))),
    ("@extensible",          AbsConstValue(PropValue(T)))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_obj, prop_obj), (loc_proto, prop_proto), (loc_contname, prop_contname_ins), (loc_contaddr, prop_contaddr_ins),
    (loc_contaddrarr, prop_contaddrarr_ins), (loc_contphonenum, prop_contphonenum_ins), (loc_contphonenumarr, prop_contphonenumarr_ins),
    (loc_contemailaddr, prop_contemailaddr_ins), (loc_contemailaddrarr, prop_contemailaddrarr_ins), (loc_contanniv, prop_contanniv_ins),
    (loc_contannivarr, prop_contannivarr_ins), (loc_contorgan, prop_contorgan_ins), (loc_contorganarr, prop_contorganarr_ins),
    (loc_contweb, prop_contweb_ins), (loc_contwebarr, prop_contwebarr_ins), (loc_types, prop_types_ins), (loc_date, prop_date_ins)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
/*      ("tizen.contactObj.getAddressBooks" -> ()),*/
      ("tizen.contactObj.getUnifiedAddressBook" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAddressBook.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(NullTop), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("readOnly", PropValue(ObjectValue(Value(BoolTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he, ctxe))
        }
        )),
      ("tizen.contactObj.getDefaultAddressBook" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAddressBook.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("readOnly", PropValue(ObjectValue(Value(BoolTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he, ctxe))
        }
        )),
      ("tizen.contactObj.getAddressBook" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENAddressBook.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(Helper.toString(v_1._1)), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("readOnly", PropValue(ObjectValue(Value(BoolTop), F, T, T)))
          val h_2 = h_1.update(l_r1, o_new)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he, ctxe))
        }
        )),
      ("tizen.contactObj.get" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENPerson.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(Helper.toString(v_1._1)), F, T, T))).
            update("displayName", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("contactCount", PropValue(ObjectValue(Value(NumTop), F, T, T))).
            update("hasPhoneNumber", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("hasEmail", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("isFavorite", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("photoURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))).
            update("ringtoneURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))).
            update("displayContactId", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val h_2 = h_1.update(l_r1, o_new)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he, ctxe))
        }
        )),
      ("tizen.contactObj.update" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENPerson.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
/*      ("tizen.contactObj.updateBatch" -> ()),*/
      ("tizen.contactObj.remove" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val es =
            if (v_1._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
/*      ("tizen.contactObj.removeBatch" -> ()),
      ("tizen.contactObj.find" -> ()),
      ("tizen.contactObj.addChangeListener" -> ()),*/
      ("tizen.contactObj.removeChangeListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val es =
            if (v_1._1._4 </ NumTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map()
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}

object TIZENAddressBook extends Tizen {
  private val name = "AddressBook"
  /* predefined locations */
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_proto, prop_proto)
  )
  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",                  AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto",                  AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",             AbsConstValue(PropValue(T))),
    ("get",                     AbsBuiltinFunc("tizen.AddressBook.get", 1)),
    ("add",                     AbsBuiltinFunc("tizen.AddressBook.add", 1)),
    ("addBatch",                AbsBuiltinFunc("tizen.AddressBook.addBatch", 3)),
    ("update",                  AbsBuiltinFunc("tizen.AddressBook.update", 1)),
    ("updateBatch",             AbsBuiltinFunc("tizen.AddressBook.updateBatch", 3)),
    ("remove",                  AbsBuiltinFunc("tizen.AddressBook.remove", 1)),
    ("removeBatch",             AbsBuiltinFunc("tizen.AddressBook.removeBatch", 3)),
    ("find",                    AbsBuiltinFunc("tizen.AddressBook.find", 4)),
    ("addChangeListener",       AbsBuiltinFunc("tizen.AddressBook.addChangeListener", 2)),
    ("removeChangeListener",    AbsBuiltinFunc("tizen.AddressBook.removeChangeListener", 1)),
    ("getGroup",                AbsBuiltinFunc("tizen.AddressBook.getGroup", 1)),
    ("addGroup",                AbsBuiltinFunc("tizen.AddressBook.addGroup", 1)),
    ("updateGroup",             AbsBuiltinFunc("tizen.AddressBook.updateGroup", 1)),
    ("removeGroup",             AbsBuiltinFunc("tizen.AddressBook.removeGroup", 1)),
    ("getGroups",               AbsBuiltinFunc("tizen.AddressBook.getGroups", 0))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      ("tizen.AddressBook.get" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val addr3 = cfg.getAPIAddress(addr_env, 2)
          val l_r1 = addrToLoc(addr1, Recent)
          val l_r2 = addrToLoc(addr2, Recent)
          val l_r3 = addrToLoc(addr3, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)
          val (h_3, ctx_3)  = Helper.Oldify(h_2, ctx_2, addr3)
          val v_1 = getArgValue(h_3, ctx_3, args, "0")

          val o_arr1 = Helper.NewArrayObject(UInt)
          val o_arr2 = o_arr1.update("@default_number", PropValue(ObjectValue(Value(StrTop), T, T, T)))
          val o_arr3 = Helper.NewArrayObject(UInt)
          val o_arr4 = o_arr3.update("@default_number", PropValue(ObjectValue(Value(StrTop), T, T, T)))
          val h_4 = h_3.update(l_r1, o_arr2).update(l_r2, o_arr4)
          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENContact.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(Helper.toString(v_1._1)), F, T, T))).
            update("personId", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))).
            update("addressBookId", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))).
            update("lastUpdated", PropValue(ObjectValue(Value(PValue(NullTop), LocSet(TIZENcontactObj.loc_date)), F, T, T))).
            update("isFavorite", PropValue(ObjectValue(Value(BoolTop), F, T, T))).
            update("name", PropValue(ObjectValue(Value(PValue(NullTop), LocSet(TIZENcontactObj.loc_contname)), T, T, T))).
            update("addresses", PropValue(ObjectValue(Value(TIZENcontactObj.loc_contaddrarr), T, T, T))).
            update("photoURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("phoneNumbers", PropValue(ObjectValue(Value(TIZENcontactObj.loc_contphonenumarr), T, T, T))).
            update("emails", PropValue(ObjectValue(Value(TIZENcontactObj.loc_contemailaddrarr), T, T, T))).
            update("birthday", PropValue(ObjectValue(Value(PValue(NullTop), LocSet(TIZENcontactObj.loc_date)), T, T, T))).
            update("anniversaries", PropValue(ObjectValue(Value(TIZENcontactObj.loc_contannivarr), T, T, T))).
            update("organizations", PropValue(ObjectValue(Value(TIZENcontactObj.loc_contorganarr), T, T, T))).
            update("notes", PropValue(ObjectValue(Value(l_r1), T, T, T))).
            update("urls", PropValue(ObjectValue(Value(TIZENcontactObj.loc_contwebarr), T, T, T))).
            update("ringtoneURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("groupIds", PropValue(ObjectValue(Value(l_r2), T, T, T)))

          val h_5 = h_4.update(l_r3, o_new)
          ((Helper.ReturnStore(h_5, Value(l_r3)), ctx_3), (he, ctxe))
        }
        )),
      ("tizen.AddressBook.add" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENContact.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val h_1 = v_1._2.foldLeft(h)((_h, l) => {
            _h + Helper.PropStore(_h, l, AbsString.alpha("id"), Value(PValue(StrTop)))
          })
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
/*            ("tizen.AddressBook.addBatch" -> ()),*/
      ("tizen.AddressBook.update" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENContact.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
/*            ("tizen.AddressBook.updateBatch" -> ()),*/
      ("tizen.AddressBook.remove" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val es =
            if (v_1._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      /*("tizen.AddressBook.removeBatch" -> ()),
      ("tizen.AddressBook.find" -> ()),
      ("tizen.AddressBook.addChangeListener" -> ()),*/
      ("tizen.AddressBook.removeChangeListener" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val es =
            if (v_1._1._4 </ NumTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.AddressBook.getGroup" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val l_r1 = addrToLoc(addr1, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val v_1 = getArgValue(h_1, ctx_1, args, "0")

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENContactGroup.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(Helper.toString(v_1._1)), F, T, T))).
            update("addressBookId", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), T, T, T))).
            update("ringtoneURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("photoURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("readOnly", PropValue(ObjectValue(Value(StrTop), F, T, T)))

          val h_2 = h_1.update(l_r1, o_new)
          ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he, ctxe))
        }
        )),
      ("tizen.AddressBook.addGroup" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENContactGroup.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val h_1 = v_1._2.foldLeft(h)((_h, l) => {
            _h + Helper.PropStore(_h, l, AbsString.alpha("id"), Value(PValue(StrTop)))
          })
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h_1, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.AddressBook.updateGroup" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val (b_1, es) = TizenHelper.instanceOf(h, v_1, Value(TIZENContactGroup.loc_proto))
          val es_1 =
            if (b_1._1._3 <= F) Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot

          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es ++ es_1)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.AddressBook.removeGroup" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val v_1 = getArgValue(h, ctx, args, "0")
          val es =
            if (v_1._1._5 </ StrTop)
              Set[WebAPIException](TypeMismatchError)
            else TizenHelper.TizenExceptionBot
          val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es)
          ((h, ctx), (he + h_e, ctxe + ctx_e))
        }
        )),
      ("tizen.AddressBook.getGroups" -> (
        (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
          val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
          val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
          if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
          val addr_env = set_addr.head
          val addr1 = cfg.getAPIAddress(addr_env, 0)
          val addr2 = cfg.getAPIAddress(addr_env, 1)
          val l_r1 = addrToLoc(addr1, Recent)
          val l_r2 = addrToLoc(addr2, Recent)
          val (h_1, ctx_1)  = Helper.Oldify(h, ctx, addr1)
          val (h_2, ctx_2)  = Helper.Oldify(h_1, ctx_1, addr2)

          val o_new = ObjEmpty.
            update("@class", PropValue(AbsString.alpha("Object"))).
            update("@proto", PropValue(ObjectValue(Value(TIZENContactGroup.loc_proto), F, F, F))).
            update("@extensible", PropValue(T)).
            update("@scope", PropValue(Value(NullTop))).
            update("@hasinstance", PropValue(Value(NullTop))).
            update("id", PropValue(ObjectValue(Value(StrTop), F, T, T))).
            update("addressBookId", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), F, T, T))).
            update("name", PropValue(ObjectValue(Value(StrTop), T, T, T))).
            update("ringtoneURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("photoURI", PropValue(ObjectValue(Value(PValue(UndefBot, NullTop, BoolBot, NumBot, StrTop)), T, T, T))).
            update("readOnly", PropValue(ObjectValue(Value(StrTop), F, T, T)))
          val h_3 = h_2.update(l_r1, o_new)
          val o_arr = Helper.NewArrayObject(UInt)
          val o_arr2 = o_arr.update("@default_number", PropValue(ObjectValue(Value(l_r1), T, T, T)))
          val h_4 = h_3.update(l_r2, o_arr2)

          ((Helper.ReturnStore(h_4, Value(l_r2)), ctx_2), (he, ctxe))
        }
        ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map()
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}

object TIZENPerson extends Tizen {
  private val name = "Person"
  /* predefined locations */
  val loc_proto = newPredefLoc(name + "Proto")

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_proto, prop_proto)
  )
  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class",                  AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto",                  AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",             AbsConstValue(PropValue(T))),
    ("link",                     AbsBuiltinFunc("tizen.Person.link", 1)),
    ("unlink",                     AbsBuiltinFunc("tizen.Person.unlink", 1))
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
      /*      ("tizen.Person.link" -> ()),
            ("tizen.Person.unlink" -> ())*/
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map()
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}