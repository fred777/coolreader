package org.coolreader.crengine;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.coolreader.CoolReader;
import org.coolreader.R;

import java.util.ArrayList;

public class BookInfoEditDialog extends BaseDialog {
	private CoolReader mActivity;
	private BookInfo mBookInfo;
	private FileInfo mParentDir;
	private LayoutInflater mInflater;
	private int mWindowSize;
	private boolean mIsRecentBooksItem;
	public BookInfoEditDialog(CoolReader activity, FileInfo baseDir, BookInfo book, boolean isRecentBooksItem)
	{
		super(activity, null, false, false);
		this.mParentDir = baseDir;
		DisplayMetrics outMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		this.mWindowSize = Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
		this.mActivity = activity;
		this.mBookInfo = book;
		this.mIsRecentBooksItem = isRecentBooksItem;
		if(getWindow().getAttributes().softInputMode==WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
		    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
	}

	@Override
	protected void onCreate() {
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        super.onCreate();
		L.v("BookInfoEditDialog is created");
	}

	class AuthorItem {
		EditText editor;
		String value;
	}
	class AuthorList {
		String oldValue;
		ArrayList<AuthorItem> authorItems = new ArrayList<>();
		ViewGroup parent;
		void adjustEditors(AuthorItem item, boolean empty) {
			int index = authorItems.indexOf(item);
			if (empty) {
				// remove extra empty lines
				for (int i=authorItems.size() - 1; i >= 0; i--) {
					if (index == i)
						continue;
					AuthorItem v = authorItems.get(i);
					if (v.value.length() == 0) {
						parent.removeView(v.editor);
						authorItems.remove(i);
					}
				}
			} else {
				// add one empty line, if none
				boolean found = false;
				for (int i=authorItems.size() - 1; i >= 0; i--) {
					if (index == i)
						continue;
					AuthorItem v = authorItems.get(i);
					if (v.value.length() == 0) {
						found = true;
					}
				}
				if (!found) {
					add("");
				}
			}
		}
		void add(String value) {
			final AuthorItem item = new AuthorItem();
			item.editor = new EditText(getContext());
			item.value = value != null ? value : "";
			item.editor.setText(value != null ? value : "");
			//item.editor.setFocusableInTouchMode(false);
			authorItems.add(item);
			parent.addView(item.editor);
			item.editor.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					boolean oldValueEmpty = item.value == null || item.value.trim().length() == 0;
					item.value = String.valueOf(s).trim();
					boolean newValueEmpty = item.value == null || item.value.trim().length() == 0;
					if (oldValueEmpty != newValueEmpty) {
						adjustEditors(item, newValueEmpty);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		}
		public AuthorList(ViewGroup parent, String authors) {
			this.oldValue = authors;
			this.parent = parent;
	        parent.removeAllViews();
	        if (authors != null) {
		        String[] list = authors.split("\\|");
		        for (String author : list)
		        	add(author);
	        }
			add("");
		}
		public String getAuthorsList() {
			StringBuilder buf = new StringBuilder();
			for (AuthorItem item : authorItems) {
				String author = item.value != null ? item.value.trim() : "";
				if (author.length() > 0) {
					if (buf.length() > 0)
						buf.append("|");
					buf.append(author);
				}
			}
			return buf.toString();
		}
	}

	static class ProgressDrawable extends Drawable {

		int progress;
		int dx;
		int dy;
		public ProgressDrawable(int dx, int dy, int progress) {
			this.dx = dx;
			this.dy = dy;
			this.progress = progress;
		}

		@Override
		public int getIntrinsicHeight() {
			return dy;
		}

		@Override
		public int getIntrinsicWidth() {
			return dx;
		}

		private void drawRect(Canvas canvas, Rect rc, Paint paint) {
			canvas.drawRect(rc.left, rc.top, rc.right, rc.top + 1, paint);
			canvas.drawRect(rc.left, rc.bottom - 1, rc.right, rc.bottom, paint);
			canvas.drawRect(rc.left, rc.top + 1, rc.left + 1, rc.bottom - 1, paint);
			canvas.drawRect(rc.right - 1, rc.top + 1, rc.right, rc.bottom - 1, paint);
		}

		private void shrink(Rect rc, int by) {
			rc.top += by;
			rc.bottom -= by;
			rc.left += by;
			rc.right -= by;
		}

		@Override
		public void draw(Canvas canvas) {
			Rect bounds = getBounds();
			if (progress >= 0 && progress <= 10000) {
				Rect rc = new Rect(bounds);
				Paint light = new Paint();
				light.setColor(0xC0FFFFFF);
				Paint dark = new Paint();
				dark.setColor(0xC0404040);
				drawRect(canvas, rc, light);
				shrink(rc, 1);
				drawRect(canvas, rc, dark);
				shrink(rc, 2);
				int x = rc.width() * progress / 10000 + rc.left;
				Rect rc1 = new Rect(rc);
				rc1.right = x;
				canvas.drawRect(rc1, light);
				Rect rc2 = new Rect(rc);
				rc2.left = x;
				canvas.drawRect(rc2, dark);
			}
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSPARENT;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}
	}

    LinearLayout mainView;
	ScrollView scrollView;
    EditText edTitle;
    EditText edLanguage;
    EditText edSeriesName;
    EditText edSeriesNumber;
    TextView lblGenres;
	EditText edGenres;
    EditText edDescription;
	AuthorList authors;
	RatingBar rbBookRating;
	RadioGroup rgState;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mInflater = LayoutInflater.from(getContext());
        FileInfo file = mBookInfo.getFileInfo();
        mainView = (LinearLayout) mInflater.inflate(R.layout.book_info_edit_dialog, null);

        ImageButton btnBack = mainView.findViewById(R.id.base_dlg_btn_back);
        btnBack.setOnClickListener(v -> onNegativeButtonClick());
        ImageButton btnOpenBook = mainView.findViewById(R.id.btn_open_book);
        btnOpenBook.setOnClickListener(v -> onPositiveButtonClick());
        ImageButton btnDeleteBook = mainView.findViewById(R.id.book_delete);
        btnDeleteBook.setOnClickListener(v -> {
			mActivity.askDeleteBook(mBookInfo.getFileInfo());
			dismiss();
		});

        scrollView = mainView.findViewById(R.id.book_scrollview);
        edTitle = mainView.findViewById(R.id.book_title);
        edLanguage = mainView.findViewById(R.id.book_lang);
        edSeriesName = mainView.findViewById(R.id.book_series_name);
        edSeriesNumber = mainView.findViewById(R.id.book_series_number);
        lblGenres = mainView.findViewById(R.id.lbl_book_genres);
        edGenres = mainView.findViewById(R.id.book_genres);
        edDescription = mainView.findViewById(R.id.book_description);

        rbBookRating = mainView.findViewById(R.id.book_rating);
        rgState = mainView.findViewById(R.id.book_state);
        int state = file.getReadingState();
        int[] stateButtons = new int[] {R.id.book_state_new, R.id.book_state_toread, R.id.book_state_reading, R.id.book_state_finished};
        rgState.check(state >= 0 && state < stateButtons.length ? stateButtons[state] : R.id.book_state_new);

        final ImageView image = mainView.findViewById(R.id.book_cover);
        image.setOnClickListener(v -> {
			// open book
			onPositiveButtonClick();
		});
        int w = mWindowSize * 4 / 10;
        int h = w * 4 / 3;
        image.setMinimumHeight(h);
        image.setMaxHeight(h);
        image.setMinimumWidth(w);
        image.setMaxWidth(w);
        Bitmap bmp = Bitmap.createBitmap(w, h, Config.RGB_565);
        Services.getCoverpageManager().drawCoverpageFor(mActivity.getDB(), file, bmp, false, (file1, bitmap) -> {
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			image.setImageDrawable(drawable);
		});

        final ImageView progress = mainView.findViewById(R.id.book_progress);
        int percent = -1;
        Bookmark bmk = mBookInfo.getLastPosition();
        if (bmk != null)
        	percent = bmk.getPercent();
        if (percent >= 0 && percent <= 10000) {
        	progress.setMinimumWidth(w);
        	progress.setMaxWidth(w);
        	progress.setMinimumHeight(8);
        	progress.setMaxHeight(8);
        } else {
        	progress.setMinimumWidth(w);
        	progress.setMaxWidth(w);
        	progress.setMinimumHeight(0);
        	progress.setMaxHeight(0);
        }
        progress.setImageDrawable(new ProgressDrawable(w, 8, percent));

        edTitle.setText(file.title);
        //edAuthor.setText(file.authors);
        edLanguage.setText(Engine.getHumanReadableLocaleName(file.language));
        edSeriesName.setText(file.series);
        if (file.series != null && file.series.trim().length() > 0 && file.seriesNumber > 0)
        	edSeriesNumber.setText(String.valueOf(file.seriesNumber));
        else
            edSeriesNumber.setText("");
        if (DocumentFormat.FB2 == file.format) {
            if (file.genres != null && file.genres.length() > 0) {
                // genre codes separated by "|", see MainDB.READ_FILEINFO_FIELDS:
                StringBuilder genres = new StringBuilder();
                String[] parts = file.genres.split("\\|");
                for (String code : parts) {
                    code = code.trim();
                    if (code.length() > 0) {
                        if (genres.length() > 0)
                            genres.append("\n");
                        genres.append(Services.getGenresCollection().translate(code));
                    }
                }
                edGenres.setText(genres.toString());
                lblGenres.setVisibility(View.VISIBLE);
                edGenres.setVisibility(View.VISIBLE);
            }
        }
        if (file.description != null && file.description.length() > 0) {
            edDescription.setText(file.description);
            edDescription.setVisibility(View.VISIBLE);
        }
        LinearLayout llBookAuthorsList = mainView.findViewById(R.id.book_authors_list);
        authors = new AuthorList(llBookAuthorsList, file.authors);
        rbBookRating.setRating(file.getRate());

    	ImageButton btnRemoveRecent = mainView.findViewById(R.id.book_recent_delete);
    	ImageButton btnOpenFolder = mainView.findViewById(R.id.book_folder_open);
        if (mIsRecentBooksItem) {
        	btnRemoveRecent.setOnClickListener(v -> {
				mActivity.askDeleteRecent(mBookInfo.getFileInfo());
				dismiss();
			});
        	btnOpenFolder.setOnClickListener(v -> {
				mActivity.showDirectory(mBookInfo.getFileInfo());
				dismiss();
			});
        } else {
        	ViewGroup parent = ((ViewGroup)btnRemoveRecent.getParent());
        	parent.removeView(btnRemoveRecent);
        	parent.removeView(btnOpenFolder);
        }

        setView(mainView);
	}

	private void save() {
		L.d("BookInfoEditDialog.save()");

        FileInfo file = mBookInfo.getFileInfo();
        boolean modified = false;
        modified = file.setTitle(edTitle.getText().toString().trim()) || modified;
        modified = file.setAuthors(authors.getAuthorsList()) || modified;
        modified = file.setSeriesName(edSeriesName.getText().toString().trim()) || modified;
        int number = 0;
        if (file.series != null && file.series.length() > 0) {
    	    String numberString = edSeriesNumber.getText().toString().trim();
    	    try {
    	    	number = Integer.parseInt(numberString);
    	    } catch (NumberFormatException e) {
    	    	// ignore
    	    }
        }
        modified = file.setSeriesNumber(number) || modified;
        int rate =(int)(rbBookRating.getRating() + 0.5f);
        if (rate >=0 && rate <= 5)
            modified = file.setRate(rate) || modified;
        int state = FileInfo.STATE_NEW;
		int currentStateId = rgState.getCheckedRadioButtonId();
		if (currentStateId == R.id.book_state_new)
			state = FileInfo.STATE_NEW;
		else if (currentStateId == R.id.book_state_toread)
			state = FileInfo.STATE_TO_READ;
		else if (currentStateId == R.id.book_state_reading)
			state = FileInfo.STATE_READING;
		else if (currentStateId == R.id.book_state_finished)
			state = FileInfo.STATE_FINISHED;
        modified = file.setReadingState(state) || modified;
        if (modified) {
        	mActivity.getDB().saveBookInfo(mBookInfo);
        	mActivity.getDB().flush();
        	BookInfo bi = Services.getHistory().getBookInfo(file);
        	if (bi != null)
        		bi.getFileInfo().setFileProperties(file);
        	mParentDir.setFile(file);
        	mActivity.directoryUpdated(mParentDir, file);
        }
	}

	@Override
	protected void onPositiveButtonClick() {
		save();
		mActivity.loadDocument(mBookInfo.getFileInfo(), null, () -> {
			// error occured
			// ignoring
		}, true);
		super.onPositiveButtonClick();
	}

	@Override
	protected void onNegativeButtonClick() {
		save();
		super.onNegativeButtonClick();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_PAGE_DOWN == keyCode) {
			scrollView.pageScroll(View.FOCUS_DOWN);
			return true;
		} else if (KeyEvent.KEYCODE_PAGE_UP == keyCode) {
			scrollView.pageScroll(View.FOCUS_UP);
			return true;
		} else if (KeyEvent.KEYCODE_BACK == keyCode) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			dismiss();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
