package org.placebooks.client.ui.pages.views;

import org.placebooks.client.PlaceBooks;
import org.placebooks.client.controllers.GroupController;
import org.placebooks.client.controllers.ItemController;
import org.placebooks.client.model.Shelf;
import org.placebooks.client.ui.UIMessages;
import org.placebooks.client.ui.dialogs.PlaceBookDialog;
import org.placebooks.client.ui.items.ImageItem;
import org.placebooks.client.ui.pages.PlaceBookPage;
import org.placebooks.client.ui.views.PlaceBookShelf;
import org.placebooks.client.ui.widgets.AndroidLink;
import org.wornchaos.views.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class KioskGroupView extends PageView implements View<Shelf>
{
	interface PlaceBookLibraryUiBinder extends UiBinder<Widget, KioskGroupView>
	{
	}

	private static final UIMessages uiMessages = GWT.create(UIMessages.class);

	private static PlaceBookLibraryUiBinder uiBinder = GWT.create(PlaceBookLibraryUiBinder.class);

	@UiField
	Label title;
	
	@UiField
	AndroidLink android;	

	@UiField
	Label description;

	@UiField
	SimplePanel imagePanel;

	@UiField
	Image qrcode;
	
	@UiField
	PlaceBookShelf shelf;

	private final GroupController controller = new GroupController();

	public KioskGroupView(final String id)
	{
		controller.load(id);
	}

	@Override
	public Widget createView()
	{
		final Widget library = uiBinder.createAndBindUi(this);

		Window.setTitle(uiMessages.placebooksLibrary());

		controller.add(this);
		controller.add(shelf);

		return library;
	}
	
	@UiHandler("qrcode")
	void showQRCode(ClickEvent event)
	{
		PlaceBookDialog dialog = new PlaceBookDialog()
		{
			
		};
		dialog.setTitle("QR Code");
		dialog.setWidget(new Image(PlaceBooks.getServer().getHostURL() + "qrcode?type=group&id=" + controller.getItem().getGroup().getId()));
		dialog.show();
	}

	@Override
	public void itemChanged(final Shelf shelf)
	{
		title.setText(shelf.getGroup().getTitle());
		description.setText(shelf.getGroup().getDescription());

		this.shelf.setType(PlaceBookPage.Type.kiosk);
		
		qrcode.setUrl(PlaceBooks.getServer().getHostURL() + "qrcode?type=group&id=" + controller.getItem().getGroup().getId());
		
		android.setPackage("org.placebooks", PlaceBooks.getServer().getHostURL() + "group/" + controller.getItem().getGroup().getId());		
		
		if (imagePanel.getWidget() == null)
		{
			final ItemController itemController = new ItemController(shelf.getGroup().getImage(),
					controller, false);
			imagePanel.setWidget(new ImageItem(itemController));
		}
		else
		{
			((ImageItem) imagePanel.getWidget()).itemChanged(shelf.getGroup().getImage());
		}
	}
}