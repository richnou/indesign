package org.odfi.indesign.core.brain.artifact

import com.idyria.osi.tea.compile.ClassDomain
import org.odfi.indesign.core.artifactresolver.AetherResolver
import org.odfi.indesign.core.brain.ExternalBrainRegion
import org.odfi.indesign.core.brain.Brain
import java.io.File
import java.util.jar.JarFile
import java.util.jar.JarEntry
import scala.collection.JavaConversions._
import org.odfi.indesign.core.module.IndesignModule
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.artifact.Artifact

class ArtifactExternalRegion(val gid: String, val aid: String, val version: String, val classifier: String = "jar") extends ExternalBrainRegion with ArtifactRegion {

  //-- ID and so
  override def getId = s"$gid:$aid:$version"
  override def getName  = aid

  def getRegionPath: String = {
    AetherResolver.resolveArtifact(gid, aid, version, classifier) match {
      case Some(art) => art.getFile.getAbsolutePath
      case None => "-"
    }
  }

  def getRegionArtifact: Artifact = new DefaultArtifact(gid, aid, classifier, version)

  def getRegionDependencies = AetherResolver.resolveDependencies(gid, aid, version, scope = "compile").map ( r => r.getArtifact)

  //-- Create Class Domain
  // var classdomain = new ClassDomain

  this.onRebuildClassDomain {
    classdomain match {
      case Some(cd) =>
        var deps = AetherResolver.resolveArtifactAndDependenciesClasspath(gid, aid, version, scope = "runtime")
        deps.foreach {
          url => classdomain.get.addURL(url)
        }
      case None =>
    }

  }

  def updateClassDomain: Unit = {
    this.@->("rebuild")
  }

  //-- Load 
  def loadRegionClass(cl: String) = {
    updateClassDomain
    classdomain match {
      case Some(cd) =>
        ESome(Brain.createRegion(cd, cl))
      case None =>
        ENone
    }

  }

  this.onInit {
    updateClassDomain
  }

  //-- Discover
  override def discoverRegions: List[String] = {

    var jf = new File(this.getRegionPath)
    (jf.exists && jf.getName.endsWith(".jar")) match {
      case true =>

        // Open Jar File
        var jar = new JarFile(jf)
        jar.entries().collect {
          case entry if (entry.getName.endsWith("$.class")) =>
            //println("Entry: "+entry)
            Brain.getObject(this.classdomain.get, entry.getName.replace("/", ".").replace(".class", "")) match {
              case Some(obj) =>
                classOf[IndesignModule].isInstance(obj) match {
                  case true => Some(obj.getClass.getCanonicalName)
                  case false => None
                }
              case None => None
            }

        }.collect { case rno if (rno.isDefined) => rno.get }.toList

      case false => List()
    }

  }

}