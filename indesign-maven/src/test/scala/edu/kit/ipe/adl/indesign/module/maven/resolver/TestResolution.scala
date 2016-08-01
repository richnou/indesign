package edu.kit.ipe.adl.indesign.module.maven.resolver

import edu.kit.ipe.adl.indesign.core.artifactresolver.AetherResolver

object TestResolution extends App {
  
  
  AetherResolver.session.setWorkspaceReader( MavenProjectIndesignWorkspaceReader)
  
  var res = AetherResolver.
  resolveArtifactAndDependencies("com.idyria.osi.wsb","wsb-webapp","2.0.0-SNAPSHOT")
  
  println(s"Res: "+res)
  res.foreach {
    p => 
      println("F: "+p.getFile)
  }
  
}