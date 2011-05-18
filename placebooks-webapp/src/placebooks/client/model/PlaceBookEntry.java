package placebooks.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public class PlaceBookEntry extends JavaScriptObject
{
	public static final native PlaceBookEntry parse(final String json) /*-{ return eval('(' + json + ')'); }-*/;

	protected PlaceBookEntry()
	{
	}

	public final native String getDescription() /*-{ return this.description; }-*/;

	public final native String getKey() /*-{ return this.key; }-*/;

	public final native int getNumItems() /*-{ return this.numItems; }-*/;

	public final native String getOwner() /*-{ return this.owner; }-*/;

	public final native String getPackagePath() /*-{ return this.packagePath; }-*/;

	public final native String getTitle() /*-{ return this.title; }-*/;
}