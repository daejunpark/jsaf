/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.debug.commands

import kr.ac.kaist.jsaf.analysis.typing.debug.DebugConsole
import kr.ac.kaist.jsaf.analysis.typing.domain.{DomainPrinter, parseLocName}
import kr.ac.kaist.jsaf.analysis.cfg.Block

class CmdPrint extends Command {
  override val name = "print"
  override val info: String = "Print out various information."

  override def help(): Unit = {
    System.out.println("usage: print allstate ({keyword})")
    System.out.println("       print state ({keyword})")
    System.out.println("       print loc {LocName}")
    System.out.println("       print fid {functionID}")
    System.out.println("       print worklist")
    System.out.println("       print (cmd|command)")
  }

  def grep(key: String, txt: String): String = {
    val list = txt.split("\n")
    list.foldLeft("")((txt, line) => {
      if (line.contains(key)) txt + line + "\n"
      else txt
    })
  }

  override def run(c: DebugConsole, args: Array[String]): Unit = {
    try {
      val subcmd = args(0)
      subcmd.toLowerCase match {
        case "allstate" => {
          val key =
            if (args.length > 1) Some(args(1))
            else None

          val inS = c.readTable(c.current)
          val heap_1 = DomainPrinter.printHeap(0, inS._1, c.getCFG, 3)
          val heap_2 = key match {
            case Some(k) => grep(k, heap_1)
            case None => heap_1
          }
          System.out.println(heap_2)
          System.out.println(DomainPrinter.printContext(0, inS._2))
        }
        case "state" => {
          val key =
            if (args.length > 1) Some(args(1))
            else None

          val inS = c.readTable(c.current)
          val heap_1 = DomainPrinter.printHeap(0, inS._1, c.getCFG)
          val heap_2 = key match {
            case Some(k) => grep(k, heap_1)
            case None => heap_1
          }
          System.out.println(heap_2)
          System.out.println(DomainPrinter.printContext(0, inS._2))
        }
        case "loc" if args.length > 1 => {
          val arg1 = args(1)
          val sloc = parseLocName(arg1)
          sloc match {
            case Some(loc) => {
              val inS = c.readTable(c.current)
              val o = inS._1(loc)
              val name = DomainPrinter.printLoc(loc)
              System.out.println(name + " -> ")
              System.out.println(DomainPrinter.printObj(4+name.length, o))
            }
            case None => {
              System.err.println("cannot find: "+arg1)
            }
          }
        }
        case "worklist" => {
          System.out.println("* Worklist set")
          c.getWorklist.getWorkList.foreach(w => {
            System.out.println("["+w._1+"] "+w._2)
          })
        }
        case "cmd" | "command" => {
          val cp = c.current

          c.getCFG.getCmd(cp._1) match {
            case Block(insts) =>
              System.out.println("- Command")
              for (inst <- insts) {
                System.out.println("    [" + inst.getInstId + "] " + inst.toString)
              }
              System.out.println()
            case _ => System.out.println("- Nothing")
          }
        }
        case "fid" if args.length > 1 => {
          val arg1 = args(1)
          try {
            val fid = arg1.toInt
            val info = c.getCFG.getFuncInfo(fid)
            val name = c.getCFG.getFuncName(fid)
            val filename = info.getSpan.getBegin.getFileName
            val begin = info.getSpan.getBegin.getLine
            val end = info.getSpan.getEnd.getLine

            System.out.println("Function name: "+name)
            System.out.println("%d~%d @%s".format(begin, end, filename))
          } catch {
            case e: NumberFormatException => System.out.println("fid must be integer.")
            case e: NoSuchElementException => System.out.println("unknown fid: "+arg1)
          }
        }
        case _ => {
          System.err.println("Illegal arguments: "+subcmd)
        }
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException => help()
    }
  }
}
