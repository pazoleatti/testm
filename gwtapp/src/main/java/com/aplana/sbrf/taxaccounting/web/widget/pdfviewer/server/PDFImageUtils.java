package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * Вспомогательная утилита для реализации серверной части компонента PDFViewer
 * 
 * @author sgoryachkin
 * 
 */
public final class PDFImageUtils {

	private PDFImageUtils() {
	}

	/**
	 * Пишем в image изображение страницы документа
	 * 
	 * @param pdf
	 * @param image
	 * @param pageIndex
	 * @param imageFormat
	 * @param imageResolution
	 */
	public static void pDFPageToImage(InputStream pdf, OutputStream image,
			int pageIndex, String imageFormat, int imageResolution) {

		PDDocument pdDocument = null;
		try {
			pdDocument = PDDocument.load(pdf);

			List<?> pages = pdDocument.getDocumentCatalog().getAllPages();
			PDPage page = (PDPage) pages.get(pageIndex);

			BufferedImage bufferedImage = page.convertToImage(
					BufferedImage.TYPE_BYTE_GRAY, imageResolution);
			if (!ImageIOUtil.writeImage(bufferedImage, imageFormat, image)) {
				throw new RuntimeException(
						"Неудалось сформировать изображение для страницы "
								+ pageIndex);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Неудалось сформировать изображение для страницы "
							+ pageIndex, e);
		} finally {
			if (pdDocument != null) {
				try {
					pdDocument.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	/**
	 * Получаем количество страниц PDF документа
	 * 
	 * @param pdf
	 * @return
	 */
	public static int getPageNumber(InputStream pdf) {
		PDDocument pdDocument = null;
		try {
			pdDocument = PDDocument.load(pdf);
			return pdDocument.getDocumentCatalog().getAllPages().size();
		} catch (Exception e) {
			throw new RuntimeException("Неудалось получить количество страниц",
					e);
		} finally {
			if (pdDocument != null) {
				try {
					pdDocument.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}
}
