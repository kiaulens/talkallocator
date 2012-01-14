package org.obraun.talkallocator

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import mapper._
import model._


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Bootable {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	    new StandardDBVendor("org.h2.Driver","jdbc:h2:talkallocator.db;AUTO_SERVER=TRUE",Empty,Empty)

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User, Talk)
    
    Talk.createExampleTalks()
    User.createExampleUsers()

    // where to search snippet
    LiftRules.addToPackages("org.obraun.talkallocator")
    
    val ifLoggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/index"))
    
    val ifAdmin = If(() => User.superUser_?, () => RedirectResponse("/index"))

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index", // the simple way to declare a menu
      
      // Definieren von Menüeinträgen mit dem Loc-Objekt
      // Params: Name, Link, Link-Text, 'LocParam' (Objekt der Klasse 'If')
      Menu(Loc("Add",List("add"),"Talk hinzufügen / löschen",ifAdmin)),
      Menu(Loc("Choose",List("choose"),"Talk auswählen",ifLoggedIn)),
      Menu(Loc("Users",List("users"),"Benutzerliste",ifAdmin))
    ) ::: User.sitemap // standard SiteMap für die Benutzerverwaltung hinzufügen

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
  }
}
