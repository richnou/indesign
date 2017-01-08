package edu.kit.ipe.adl.indesign.core.config

import edu.kit.ipe.adl.indesign.core.config.model.CommonConfigTrait
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import edu.kit.ipe.adl.indesign.core.config.model.HarvesterConfig
import edu.kit.ipe.adl.indesign.core.config.model.DefaultConfig
import edu.kit.ipe.adl.indesign.core.config.model.RegionConfig
import edu.kit.ipe.adl.indesign.core.config.model.CommonConfig
import com.idyria.osi.ooxoo.db.store.DocumentContainer

object Config extends BrainRegion {

  // Make this region always present
  this.root

  var implementation: Option[ConfigImplementation] = None

  def setImplementation(i: ConfigImplementation) = implementation = Some(i)

  /*def getImplementation = implementation match {
    case Some(i) => i 
    case None => th
  }*/

  def documentName(target: ConfigSupport): String = {

    target.getClass.getCanonicalName match {
      // Object
      case cn if (cn.endsWith("$")) =>
        cn.replace("$", "")
      case cn =>
        // Class
        cn.replace("$", "Object") + "_" + target.getId
    }

  }

  def getConfigFor[CS <: ConfigSupport](target: ConfigSupport): Option[CommonConfig] = {

    try {

      implementation match {
        case Some(impl) =>
          target.getClass match {

            // Harvester
            //---------------
            case cl if (classOf[Harvester].isAssignableFrom(cl)) =>

              var c = impl.getContainer("harvesters")
              c.document(documentName(target), new HarvesterConfig, true)

            // Region
            //----------------
            case cl if (classOf[BrainRegion].isAssignableFrom(cl)) =>

              var c = impl.getContainer("regions")
              c.document(documentName(target), new RegionConfig, true)

            // Main/Default
            //----------
            case cl =>
              var c = impl.getContainer("default")
              c.document(documentName(target), new DefaultConfig, true)
          }
        case None => None
      }
    } catch {
      case e: Throwable =>
        //e.printStackTrace()
        None
    }

  }
  
  // Def Builders
  //------------------
  def apply (imp: ConfigImplementation) = {
    
    this.setImplementation(imp)
    
    this
  }
  
  // Generic Database Access
  //------------------------
  
   def getContainerFor(cl:String) : Option[DocumentContainer] = {
    
    this.implementation match {
      case Some(implementation) => 
        Some(implementation.getContainer(cl))
      case None => None
    }
    
  }
  
  /**
   * Returns a container named after the class
   */
  def getContainerFor(cl:Class[_]) : Option[DocumentContainer] = getContainerFor(cl.getCanonicalName.replace("$", ""))
  
  def getContainerFor(o:AnyRef) : Option[DocumentContainer] =  this.getContainerFor(o.getClass)
}