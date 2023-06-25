import cv2
import pytesseract
from pytesseract import pytesseract
import PIL
from PIL import Image


def testThing():
    img=Image.open(r"/Users/ji_tzuoh_lin/Desktop/stuff/apps/usabo-quizzes/Parsing/imgs/USABO 17 Semifinal Final.Web/USABO 17 Semifinal Final.Web1024_1.jpg")
    data=pytesseract.image_to_boxes(img)
    print(data)

testThing()
