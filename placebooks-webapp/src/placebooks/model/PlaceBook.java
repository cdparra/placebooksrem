package placebooks.model;

import java.util.*;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;


@PersistenceCapable(identityType=IdentityType.DATASTORE)
@Extension(vendorName="datanucleus", key="mysql-engine-type", value="MyISAM")
public class PlaceBook
{
	@NotPersistent
	protected static final Logger log = 
		Logger.getLogger(PlaceBook.class.getName());

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.UUIDHEX)
	private String key;

	@Persistent
	private User owner;
	
	@Persistent
	private Date timestamp;

	@Persistent
	private Geometry geom; // Pertaining to the PlaceBook

	// TODO: Cascading deletes via dependent=true: not sure about this
	@Persistent(mappedBy="placebook")
	@javax.jdo.annotations.Element(dependent = "true") 
	private List<PlaceBookItem> items = new ArrayList<PlaceBookItem>();

	// Searchable metadata attributes, e.g., title, description, etc.
	@Persistent
	private Map<String, String> metadata = new HashMap<String, String>();

	@Persistent(dependent="true")
	private PlaceBookIndex index;

	// Make a new PlaceBook
	public PlaceBook(User owner, Geometry geom)
	{
		this.owner = owner;
		if (owner != null)
			this.owner.add(this);
		this.geom = geom;
		this.timestamp = new Date();

		log.info("Created new PlaceBook: timestamp=" 
				 + this.timestamp.toString());

	}
	
	public PlaceBook(User owner, Geometry geom, List<PlaceBookItem> items)
	{
		this(owner, geom);
		setItems(items);
	}

	public Element createConfigurationRoot(Document config)
	{
		log.info("PlaceBook.appendConfiguration(), key=" + this.getKey());
		Element root = config.createElement(PlaceBook.class.getName());
		root.setAttribute("key", this.getKey());
		root.setAttribute("owner", this.getOwner().getKey());
		
		Element timestamp = config.createElement("timestamp");
		timestamp.appendChild(config.createTextNode(
								this.getTimestamp().toString()));
		root.appendChild(timestamp);

		Element geometry = config.createElement("geometry");
		geometry.appendChild(config.createTextNode(
								this.getGeometry().toText()));
		root.appendChild(geometry);

		Iterator i = ((Set)this.getMetadata().entrySet()).iterator();
		if (i.hasNext())
		{
			Element sElem = config.createElement("metadata");

			for ( ; i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				Element elem = config.createElement(e.getKey().toString());
				elem.appendChild(
					config.createTextNode(e.getValue().toString()));
				sElem.appendChild(elem);
			}

			root.appendChild(sElem);
		}

		return root;
	}

	public void setItems(List<PlaceBookItem> items)
	{
		this.items.clear();
		this.items.addAll(items);
	}

	public List<PlaceBookItem> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	public void addItem(PlaceBookItem item) 
	{
  		items.add(item);
  		item.setPlaceBook(this);
	}

	public boolean removeItem(PlaceBookItem item)
	{
		item.setPlaceBook(null);
		return items.remove(item);
	}
	
	public void addMetadataEntry(String key, String value)
	{
		metadata.put(key, value);
	}

	public String getMetadataValue(String key)
	{
		return metadata.get(key);
	}

	public Map<String, String> getMetadata()
	{
		return Collections.unmodifiableMap(metadata);
	}

	public boolean hasMetadata()
	{
		return (!metadata.isEmpty());
	}

	public String getKey() { return key; }

	public void setOwner(User owner) { this.owner = owner; }
	public User getOwner() { return owner; }

	public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
	public Date getTimestamp() { return timestamp; }

	public void setGeometry(Geometry geom) { this.geom = geom; }
	public Geometry getGeometry() { return geom; }
}
