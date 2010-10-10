case class ContentTag(
  val name: String,
  val abbr: String
)

case class ContentItem(
  val id: String,
  val agent: String,
  val name: String,
  val value: String,
  val vtype: String,
  val uri: String,
  val position: Int,
  val relationships: List[ContentTag],
  val parent: Option[String],
  val children: List[ContentItem]
)

def getRelationships(item: Node): List[ContentTag] = {
  (item \ "relationships" \ "relationship").toList.map(rel => {
    ContentTag(
      (rel \ "name").text,
      (rel \ "abbr").text
    )
  })
}

def getItems(items: NodeSeq, parent: Option[String]): List[ContentItem] = {
  items.toList.map(item => {
    ContentItem(
      (item \ "id").text,
      (item \ "agent").text,
      (item \ "name").text,
      (item \ "value").text,
      (item \ "@vtype").text,
      (item \ "uri").text,
      (item \ "position").text match {
        case s: String if (s != "") => s.toInt
        case _ => -1
      },
      getRelationships(item),
      parent,
      getItems((item \ "items" \ "item"), Some((item \ "id").text))
    )
  })
}