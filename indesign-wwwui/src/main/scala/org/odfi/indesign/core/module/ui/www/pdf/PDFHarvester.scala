package org.odfi.indesign.core.module.ui.www.pdf

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.HarvestedFile

class PDFHarvester extends Harvester {
  
  this.onDeliverFor[HarvestedFile] {
    case f if (f.path.toFile.getName.endsWith("pdf")) => 
      gather(new PDFFile(f))
      true
  }
  
}

class PDFFile(f:HarvestedFile) extends HarvestedFile(f.path) {
  deriveFrom(f)
}