package placebooks.client.ui;

import placebooks.client.AbstractCallback;
import placebooks.client.PlaceBookService;
import placebooks.client.model.PlaceBook;
import placebooks.client.model.PlaceBookItem;
import placebooks.client.resources.Resources;
import placebooks.client.ui.items.frames.PlaceBookItemPopupFrame;
import placebooks.client.ui.menuItems.MenuItem;
import placebooks.client.ui.palette.Palette;
import placebooks.client.ui.places.PlaceBookEditorPlace;
import placebooks.client.ui.places.PlaceBookHomePlace;
import placebooks.client.ui.places.PlaceBookPreviewPlace;
import placebooks.client.ui.widget.RichTextArea;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class PlaceBookEditor extends Composite
{
	public class SaveContext extends Timer
	{
		private static final int saveDelay = 2000;

		// private boolean changed = false;
		// private boolean saving = false;
		// private boolean waiting = false;

		public void markChanged()
		{
			cancel();
			schedule(saveDelay);
			// changed = true;
		}

		@Override
		public void run()
		{
			savingPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);

			// changed = false;
			// saving = true;
			PlaceBookService.savePlaceBook(placebook, new AbstractCallback()
			{
				@Override
				public void failure(final Request request, final Response response)
				{
					markChanged();
					savingPanel.getElement().getStyle().setDisplay(Display.NONE);
				}

				@Override
				public void success(final Request request, final Response response)
				{
					updatePlaceBook(PlaceBook.parse(response.getText()));
					savingPanel.getElement().getStyle().setDisplay(Display.NONE);
				}
			});
		}
	}

	interface PlaceBookEditorUiBinder extends UiBinder<Widget, PlaceBookEditor>
	{
	}

	private static final PlaceBookEditorUiBinder uiBinder = GWT.create(PlaceBookEditorUiBinder.class);

	@UiField
	PlaceBookToolbarLogin account;

	@UiField
	Panel backPanel;

	@UiField
	Panel canvasPanel;

	@UiField
	Panel loadingPanel;

	@UiField
	Palette palette;

	@UiField
	Panel savingPanel;

	@UiField
	RichTextArea title;

	@UiField
	Label zoomLabel;

	private final PlaceBookCanvas canvas = new PlaceBookCanvas();

	private final PlaceBookInteractionHandler interactionHandler;

	private PlaceBook placebook;
	
	private final PlaceController placeController;
	
	private final PlaceBookItemPopupFrame.Factory factory = new PlaceBookItemPopupFrame.Factory();

	private SaveContext saveContext = new SaveContext();

	private int zoom = 100;

	public PlaceBookEditor(final PlaceController placeController)
	{
		initWidget(uiBinder.createAndBindUi(this));

		this.placeController = placeController;

		canvasPanel.add(canvas);
		
		Event.addNativePreviewHandler(new Event.NativePreviewHandler()
		{
			@Override
			public void onPreviewNativeEvent(final NativePreviewEvent event)
			{
				if ((event.getTypeInt() == Event.ONMOUSEDOWN || event.getTypeInt() == Event.ONMOUSEMOVE)
						&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT
						&& event.getNativeEvent().getEventTarget().toString().startsWith("<img"))
				{
					event.getNativeEvent().preventDefault();
				}
			}
		});

		interactionHandler = new PlaceBookInteractionHandler(canvas, factory, saveContext);
		interactionHandler.setupUIElements(backPanel);
		
		factory.setInteractionHandler(interactionHandler);

		Window.setTitle("PlaceBooks Editor");

		account.add(new MenuItem("Print Preview")
		{
			@Override
			public void run()
			{
				placeController.goTo(new PlaceBookPreviewPlace(getCanvas().getPlaceBook()));				
			}
		});
		account.add(new MenuItem("Publish")
		{
			@Override
			public void run()
			{
				final PopupPanel dialogBox = new PopupPanel();
				dialogBox.setGlassEnabled(true);
				dialogBox.setAnimationEnabled(true);
				final PlaceBookPublish publish = new PlaceBookPublish(placeController, canvas);
				publish.addClickHandler(new ClickHandler()
				{
					@Override
					public void onClick(ClickEvent event)
					{
						dialogBox.hide();
					}
				});
				dialogBox.add(publish);
				
				dialogBox.setStyleName(Resources.INSTANCE.style().dialog());
				dialogBox.setGlassStyleName(Resources.INSTANCE.style().dialogGlass());
				dialogBox.setAutoHideEnabled(true);

				dialogBox.getElement().getStyle().setZIndex(1000);
				dialogBox.show();
				dialogBox.center();
				dialogBox.getElement().getStyle().setTop(50, Unit.PX);
			}
		});
		
		updatePalette();
		final Timer timer = new Timer()
		{
			@Override
			public void run()
			{
				updatePalette();
			}
		};
		timer.scheduleRepeating(50000);
	}

	public PlaceBookCanvas getCanvas()
	{
		return canvas;
	}

	public PlaceBookInteractionHandler getDragHandler()
	{
		return interactionHandler;

	}

	public SaveContext getSaveContext()
	{
		return saveContext;
	}

	public void markChanged()
	{
		saveContext.markChanged();
	}

	public void updatePalette()
	{
		PlaceBookService.getPaletteItems(new AbstractCallback()
		{
			@Override
			public void failure(final Request request, final Response response)
			{
				if (response.getStatusCode() == 401)
				{
					placeController.goTo(new PlaceBookHomePlace());
				}
			}

			@Override
			public void success(final Request request, final Response response)
			{
				final JsArray<PlaceBookItem> items = PlaceBookItem.parseArray(response.getText());
				palette.setPalette(items, interactionHandler);
			}
		});
	}

	public void setPlaceBook(final PlaceBook newPlacebook)
	{
		placebook = newPlacebook;
		
		canvas.setPlaceBook(newPlacebook, factory, true);
		
		if (newPlacebook.hasMetadata("title"))
		{
			Window.setTitle(newPlacebook.getMetadata("title") + " - PlaceBooks Editor");
			title.getElement().setInnerText(newPlacebook.getMetadata("title"));
		}
		else
		{
			Window.setTitle("PlaceBooks Editor");
			title.getElement().setInnerText("No Title");
		}

		account.setUser(newPlacebook.getOwner());

		loadingPanel.setVisible(false);
		canvas.reflow();
	}

	private void updatePlaceBook(final PlaceBook newPlacebook)
	{
		if (placebook != null
				&& (placebook.getKey() == null || !placebook.getKey().equals(newPlacebook.getKey())))
		{
			canvas.updatePlaceBook(newPlacebook);
			
			PlaceBook placebook = canvas.getPlaceBook();
			placebook.setKey(newPlacebook.getKey());
			

			interactionHandler.reset();
			
			placeController.goTo(new PlaceBookEditorPlace(placebook));
		}
		else
		{
			placebook = newPlacebook;
			canvas.updatePlaceBook(newPlacebook);
			
			if (newPlacebook.hasMetadata("title"))
			{
				Window.setTitle(newPlacebook.getMetadata("title") + " - PlaceBook Editor");
				title.getElement().setInnerText(newPlacebook.getMetadata("title"));
			}
			else
			{
				Window.setTitle("PlaceBook Editor");
				title.getElement().setInnerText("No Title");
			}
	
			account.setUser(newPlacebook.getOwner());
			canvas.reflow();
		}
	}

	@Override
	protected void onAttach()
	{
		super.onAttach();
		canvas.reflow();
	}

	@UiHandler("title")
	void handleTitleEdit(final KeyUpEvent event)
	{
		canvas.getPlaceBook().setMetadata("title", title.getElement().getInnerText());
		saveContext.markChanged();
	}

	@UiHandler("zoomIn")
	void handleZoomIn(final ClickEvent event)
	{
		setZoom(zoom + 20);
	}

	@UiHandler("zoomOut")
	void handleZoomOut(final ClickEvent event)
	{
		setZoom(zoom - 20);
	}

	private void setZoom(final int zoom)
	{
		this.zoom = zoom;
		canvas.getElement().getStyle().setWidth(zoom, Unit.PCT);
		canvas.getElement().getStyle().setFontSize(zoom, Unit.PCT);
		zoomLabel.setText(zoom + "%");
		for (final PlaceBookPanel panel : canvas.getPanels())
		{
			panel.reflow();
		}
	}
}