package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import java.io.{File, FileOutputStream, OutputStream}
import com.vaadin.terminal.FileResource
import com.vaadin.ui._
import com.vaadin.ui.Window.Notification

class ContentUploader extends CustomComponent with Upload.SucceededListener with Upload.FailedListener with Upload.Receiver {
  var file: File = null
  
  val layout = new FormLayout
  setCompositionRoot(layout)
  
  // Create the Upload component.
  val upload: Upload = new Upload("Upload file", this)
  
  // Use a custom button caption instead of plain "Upload".
  // upload.setButtonCaption("Upload")
  
  // Listen for events regarding the success of upload.
  upload.addListener(this.asInstanceOf[Upload.SucceededListener])
  upload.addListener(this.asInstanceOf[Upload.FailedListener])
  
  layout.addComponent(upload)
  layout.setSpacing(true)
  
  // Callback method to begin receiving the upload.
  def receiveUpload(filename: String, MIMEType: String): OutputStream = {
    var fos: FileOutputStream = null
    
    file = new File("/tmp/uploads/" + filename)
    
    try {
      new FileOutputStream(file)
    } catch {
      case e: java.io.FileNotFoundException => e.printStackTrace()
        null
    }
  }
  
  // This is called if the upload is finished.
  def uploadSucceeded(event: Upload#SucceededEvent) {
    getWindow().showNotification("Upload succeeded", "The " + event.getFilename() +
      " file of type '" + event.getMIMEType() + "' was successfully uploaded.",
      Notification.TYPE_TRAY_NOTIFICATION)
  }
  
  // This is called if the upload fails.
  def uploadFailed(event: Upload#FailedEvent) {
    getWindow().showNotification("Upload failed", "The " + event.getFilename() +
      " file of type '" + event.getMIMEType() + "' failed to upload.",
      Notification.TYPE_TRAY_NOTIFICATION)
  }
}