package placebooks.client.ui.menuItems;

import placebooks.client.ui.PlaceBookItemWidgetFrame;
import placebooks.client.ui.widget.MenuItem;

public class FitToContentMenuItem extends MenuItem
{
	private final PlaceBookItemWidgetFrame item;

	public FitToContentMenuItem(final String title, final PlaceBookItemWidgetFrame item)
	{
		super(title);
		this.item = item;
	}

	@Override
	public boolean isEnabled()
	{
		return item.getItem().hasParameter("height");
	}

	@Override
	public void run()
	{
		item.getItem().removeParameter("height");
		item.getPanel().reflow();
		item.markChanged();
	}
}
