/*
 * Copyright (c) 2016, 2019, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "Pictures.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Pictures(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_VERSION_1_4;
    }
    return JNI_VERSION_1_8;
#else
    return JNI_VERSION_1_4;
#endif
}

static int picturesInited = 0;

// Pictures
jclass mat_jPicturesServiceClass;
jmethodID mat_jPicturesService_setResult = 0;
Pictures *_pictures;
BOOL savePhoto;


JNIEXPORT void JNICALL Java_com_gluonhq_attach_pictures_impl_IOSPicturesService_initPictures
(JNIEnv *env, jclass jClass)
{
    if (picturesInited)
    {
        return;
    }
    picturesInited = 1;
    
    mat_jPicturesServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/pictures/impl/IOSPicturesService"));
    mat_jPicturesService_setResult = (*env)->GetStaticMethodID(env, mat_jPicturesServiceClass, "setResult", "(Ljava/lang/String;Ljava/lang/String;)V");
}

void sendPicturesResult(NSString *picResult, NSString *picPath) {
    if (picResult)
    {
        const char *picChars = [picResult UTF8String];
        jstring jpic = (*env)->NewStringUTF(env, picChars);
        const char *pathChars = [picPath UTF8String];
        jstring jpath = (*env)->NewStringUTF(env, pathChars);
        (*env)->CallStaticVoidMethod(env, mat_jPicturesServiceClass, mat_jPicturesService_setResult, jpic, jpath);
        (*env)->DeleteLocalRef(env, jpic);
        (*env)->DeleteLocalRef(env, jpath);
        NSLog(@"Finished sending picture");
    } else 
    {
        (*env)->CallStaticVoidMethod(env, mat_jPicturesServiceClass, mat_jPicturesService_setResult, NULL, NULL);
    } 
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_pictures_impl_IOSPicturesService_takePicture
(JNIEnv *env, jclass jClass, jboolean jSavePhoto)
{
    savePhoto = jSavePhoto;
    _pictures = [[Pictures alloc] init];
    [_pictures takePicture];
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_pictures_impl_IOSPicturesService_selectPicture
(JNIEnv *env, jclass jClass)
{
    _pictures = [[Pictures alloc] init];
    [_pictures selectPicture];
    return;   
}

@implementation Pictures 

- (void)takePicture {
    if(![[UIApplication sharedApplication] keyWindow])
    {
        NSLog(@"key window was nil");
        return;
    }
   
    NSArray *views = [[[UIApplication sharedApplication] keyWindow] subviews];
    if(![views count]) {
        NSLog(@"views size was 0");
        return;
    }
   
    if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        NSLog(@"Device has no camera");
        return;
    }

    UIView *_currentView = views[0];
    
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = NO;
    picker.sourceType = UIImagePickerControllerSourceTypeCamera;
    
    [_currentView.window addSubview:picker.view];
    
}

- (void)selectPicture {
    if(![[UIApplication sharedApplication] keyWindow])
    {
        NSLog(@"key window was nil");
        return;
    }
   
    NSArray *views = [[[UIApplication sharedApplication] keyWindow] subviews];
    if(![views count]) {
        NSLog(@"views size was 0");
        return;
    }
   
    UIView *_currentView = views[0];
    
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = NO;
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    
    [_currentView.window addSubview:picker.view];
    
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    
    NSLog(@"Encoding and sending retrieved picture");
    UIImage *originalImage = info[UIImagePickerControllerOriginalImage];

    if (savePhoto == YES) 
    {
        NSLog(@"Saving picture...");
        UIImageWriteToSavedPhotosAlbum(originalImage, nil, nil, nil);
    }

// The original image could be too big (ie 3264x2448) and not properly rotated,
// what leads to: core.memory: GC Warning: Repeated allocation of very large block
// and even: malloc: *** mach_vm_map(size=67108864) failed (error code=3) -> NPE at
//   com.sun.prism.impl.BaseGraphics.drawTexture(BaseGraphics.java)

// Solution: limit max size to 1280x1280, and rotate properly:

    UIImage *image = [self scaleAndRotateImage:originalImage];

    NSData *imageData = UIImagePNGRepresentation(image);
    
    NSString *base64StringOfImage = [imageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
    
    NSData *originalData = UIImagePNGRepresentation(originalImage);
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd_HH-mm-ss"];
    NSString *stringFromDate = [formatter stringFromDate:[NSDate date]];

    NSString *filePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@.png",@"Image",stringFromDate]];
    [originalData writeToFile:filePath atomically:YES];

    sendPicturesResult(base64StringOfImage, filePath);

    [picker dismissViewControllerAnimated:YES completion:nil];
    [picker.view removeFromSuperview];
    [picker release];

}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {

    NSLog(@"Camera cancelled");

    NSString *result = nil;
    sendPicturesResult(result, result);

    [picker dismissViewControllerAnimated:YES completion:nil];
    [picker.view removeFromSuperview];
    [picker release];

}

- (UIImage *)scaleAndRotateImage:(UIImage *)image
{
// FIXME: hardcoded value, add it as a parameter
    int kMaxResolution = 1280; 

    CGImageRef imgRef = image.CGImage;

    CGFloat width = CGImageGetWidth(imgRef);
    CGFloat height = CGImageGetHeight(imgRef);

    CGAffineTransform transform = CGAffineTransformIdentity;
    CGRect bounds = CGRectMake(0, 0, width, height);
    if (width > kMaxResolution || height > kMaxResolution) {
        CGFloat ratio = width/height;
        if (ratio > 1) {
            bounds.size.width = kMaxResolution;
            bounds.size.height = bounds.size.width / ratio;
        }
        else {
            bounds.size.height = kMaxResolution;
            bounds.size.width = bounds.size.height * ratio;
        }
    }

    CGFloat scaleRatio = bounds.size.width / width;
    CGSize imageSize = CGSizeMake(CGImageGetWidth(imgRef), CGImageGetHeight(imgRef));
    CGFloat boundHeight;
    UIImageOrientation orient = image.imageOrientation;
    switch(orient) {

        case UIImageOrientationUp: //EXIF = 1
            transform = CGAffineTransformIdentity;
            break;

        case UIImageOrientationUpMirrored: //EXIF = 2
            transform = CGAffineTransformMakeTranslation(imageSize.width, 0.0);
            transform = CGAffineTransformScale(transform, -1.0, 1.0);
            break;

        case UIImageOrientationDown: //EXIF = 3
            transform = CGAffineTransformMakeTranslation(imageSize.width, imageSize.height);
            transform = CGAffineTransformRotate(transform, M_PI);
            break;

        case UIImageOrientationDownMirrored: //EXIF = 4
            transform = CGAffineTransformMakeTranslation(0.0, imageSize.height);
            transform = CGAffineTransformScale(transform, 1.0, -1.0);
            break;

        case UIImageOrientationLeftMirrored: //EXIF = 5
            boundHeight = bounds.size.height;
            bounds.size.height = bounds.size.width;
            bounds.size.width = boundHeight;
            transform = CGAffineTransformMakeTranslation(imageSize.height, imageSize.width);
            transform = CGAffineTransformScale(transform, -1.0, 1.0);
            transform = CGAffineTransformRotate(transform, 3.0 * M_PI / 2.0);
            break;

        case UIImageOrientationLeft: //EXIF = 6
            boundHeight = bounds.size.height;
            bounds.size.height = bounds.size.width;
            bounds.size.width = boundHeight;
            transform = CGAffineTransformMakeTranslation(0.0, imageSize.width);
            transform = CGAffineTransformRotate(transform, 3.0 * M_PI / 2.0);
            break;

        case UIImageOrientationRightMirrored: //EXIF = 7
            boundHeight = bounds.size.height;
            bounds.size.height = bounds.size.width;
            bounds.size.width = boundHeight;
            transform = CGAffineTransformMakeScale(-1.0, 1.0);
            transform = CGAffineTransformRotate(transform, M_PI / 2.0);
            break;

        case UIImageOrientationRight: //EXIF = 8
            boundHeight = bounds.size.height;
            bounds.size.height = bounds.size.width;
            bounds.size.width = boundHeight;
            transform = CGAffineTransformMakeTranslation(imageSize.height, 0.0);
            transform = CGAffineTransformRotate(transform, M_PI / 2.0);
            break;

        default:
            [NSException raise:NSInternalInconsistencyException format:@"Invalid image orientation"];

    }

    UIGraphicsBeginImageContext(bounds.size);

    CGContextRef context = UIGraphicsGetCurrentContext();

    if (orient == UIImageOrientationRight || orient == UIImageOrientationLeft) {
        CGContextScaleCTM(context, -scaleRatio, scaleRatio);
        CGContextTranslateCTM(context, -height, 0);
    }
    else {
        CGContextScaleCTM(context, scaleRatio, -scaleRatio);
        CGContextTranslateCTM(context, 0, -height);
    }

    CGContextConcatCTM(context, transform);

    CGContextDrawImage(UIGraphicsGetCurrentContext(), CGRectMake(0, 0, width, height), imgRef);
    UIImage *imageCopy = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return imageCopy;
}

@end
