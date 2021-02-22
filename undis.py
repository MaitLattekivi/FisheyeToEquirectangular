import glob

import cv2

mtx = [[402.19], [0.00], [1187.23], [0.00], [321.61], [2286.73] ,[0.00] ,[0.00], [1.00]]
dist = [[-0.15165436,  0.01863983 , 0.00101603 , 0.00184165, -0.00093491]]
w = 2160
h = 3840

images = glob.glob('pildid/*.jpg')

img = cv2.imread(images[9])
# Refining the camera matrix using parameters obtained by calibration
newcameramtx, roi = cv2.getOptimalNewCameraMatrix(mtx, dist, (w, h), 1, (w, h))

# Method 1 to undistort the image
dst = cv2.undistort(img, mtx, dist, None, newcameramtx)

# Method 2 to undistort the image
#mapx,mapy=cv2.initUndistortRectifyMap(mtx,dist,None,newcameramtx,(w,h),5)

#dst = cv2.remap(img,mapx,mapy,cv2.INTER_LINEAR)


# Displaying the undistorted image
#im = cv2.resize(dist, (960, 540))
cv2.imwrite('./pildid/pygi/undistort.jpg' , dst)
