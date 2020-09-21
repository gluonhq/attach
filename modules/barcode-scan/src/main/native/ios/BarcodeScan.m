/*
 * Copyright (c) 2016, 2020, Gluon
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

#include "BarcodeScan.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_BarcodeScan(JavaVM *vm, void *reserved)
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

static int BarcodeScanInited = 0;

// BarcodeScan
jclass mat_jScanServiceClass;
jmethodID mat_jScanService_setResult = 0;
BarcodeScan *_barcodeScan;


JNIEXPORT void JNICALL Java_com_gluonhq_attach_barcodescan_impl_IOSBarcodeScanService_initBarcodeScan
(JNIEnv *env, jclass jClass)
{
    if (BarcodeScanInited)
    {
        return;
    }
    BarcodeScanInited = 1;
    
    mat_jScanServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/barcodescan/impl/IOSBarcodeScanService"));
    mat_jScanService_setResult = (*env)->GetStaticMethodID(env, mat_jScanServiceClass, "setResult", "(Ljava/lang/String;)V");
}

void sendScanResult(NSString *scanResult) {
    const char *scanChars = [scanResult UTF8String];
    jstring arg = (*env)->NewStringUTF(env, scanChars);
    (*env)->CallStaticVoidMethod(env, mat_jScanServiceClass, mat_jScanService_setResult, arg);
    (*env)->DeleteLocalRef(env, arg);
	AttachLog(@"Finished sending scan result");
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_barcodescan_impl_IOSBarcodeScanService_startBarcodeScan
(JNIEnv *env, jclass jClass, jstring jTitle, jstring jLegend, jstring jResult)
{

    const jchar *charsTitle = (*env)->GetStringChars(env, jTitle, NULL);
    NSString *sTitle = [NSString stringWithCharacters:(UniChar *)charsTitle length:(*env)->GetStringLength(env, jTitle)];
    (*env)->ReleaseStringChars(env, jTitle, charsTitle);

    const jchar *charsLegend = (*env)->GetStringChars(env, jLegend, NULL);
    NSString *sLegend = [NSString stringWithCharacters:(UniChar *)charsLegend length:(*env)->GetStringLength(env, jLegend)];
    (*env)->ReleaseStringChars(env, jLegend, charsLegend);

    const jchar *charsResult = (*env)->GetStringChars(env, jResult, NULL);
    NSString *sResult = [NSString stringWithCharacters:(UniChar *)charsResult length:(*env)->GetStringLength(env, jResult)];
    (*env)->ReleaseStringChars(env, jResult, charsResult);

    _barcodeScan = [[BarcodeScan alloc] init];
    [_barcodeScan display:sTitle legend:sLegend resultText:sResult];
    return;
}

@implementation BarcodeScan 

AVCaptureSession *_session;
AVCaptureDevice *_device;
AVCaptureDeviceInput *_input;
AVCaptureMetadataOutput *_output;
AVCaptureVideoPreviewLayer *_prevLayer;
UINavigationItem *currentItem;
UINavigationBar *navBar;
NSString *resultString;

- (void)display:(NSString *)title legend:(NSString *)legend resultText:(NSString *)resultText
{
    if(![[UIApplication sharedApplication] keyWindow])
    {
        AttachLog(@"key window was nil");
        return;
    }
   
    // get the root view controller
    UIViewController *rootViewController = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    if(!rootViewController)
    {
        AttachLog(@"rootViewController was nil");
        return;
    }
   
    resultString = resultText;

    // get the view
    UIView *view = self.view;

    _session = [[AVCaptureSession alloc] init];
    _device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    NSError *error = nil;

    _input = [AVCaptureDeviceInput deviceInputWithDevice:_device error:&error];
    if (_input) {
        [_session addInput:_input];
    } else {
        AttachLog(@"Error: %@", error);
    }

    _output = [[AVCaptureMetadataOutput alloc] init];
    [_output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    [_session addOutput:_output];

    _output.metadataObjectTypes = [_output availableMetadataObjectTypes];

    _prevLayer = [AVCaptureVideoPreviewLayer layerWithSession:_session];
    _prevLayer.frame = view.bounds;
    _prevLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    _prevLayer.connection.videoOrientation = [self videoOrientationFromCurrentDeviceOrientation];
    [view.layer addSublayer:_prevLayer];

    CGRect sbFrame = [[UIApplication sharedApplication] statusBarFrame];
    int ofs = sbFrame.size.height;
    
    navBar = [[UINavigationBar alloc] initWithFrame:CGRectMake(0, ofs, self.view.frame.size.width, 44)];
    [navBar setBackgroundImage:[UIImage new] forBarMetrics:UIBarMetricsDefault];
    navBar.shadowImage = [UIImage new];
    navBar.translucent = YES;
    [navBar setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]}];
    
    currentItem = [[UINavigationItem alloc] init];
    if ([title length] != 0) {
        currentItem.title = title;
    }

    UIBarButtonItem *leftButton = [[UIBarButtonItem alloc] initWithTitle:@"Cancel" style:UIBarButtonItemStylePlain 
            target:self action:@selector(cancel:)];
    currentItem.leftBarButtonItem = leftButton;

    navBar.items = @[ currentItem ];
    [view addSubview:navBar];

    // show view controller
    [rootViewController presentViewController:self animated:YES completion:nil];
    [_session startRunning];
    
    if ([legend length] != 0) {
        UIAlertController *toast = [UIAlertController alertControllerWithTitle:nil message:legend preferredStyle:UIAlertControllerStyleAlert];
        [self presentViewController:toast animated:YES completion:nil];
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [NSThread sleepForTimeInterval:2.0f];   
            dispatch_async(dispatch_get_main_queue(), ^{
                [toast dismissViewControllerAnimated:YES completion:nil];
            });
        });
    }
}

// hide barcodeScan preview and view controller
- (IBAction)cancel:(id)sender
{
    AttachLog(@"Scan cancelled");
    NSString *result = nil;
    sendScanResult(result);
    [self end];
}

- (void)end
{
    if([_session isRunning])
    {
        [_session stopRunning];
    }
    [_session removeInput:_input];
    [_session removeOutput:_output];
    [_prevLayer removeFromSuperlayer];
    [currentItem release];
    currentItem = nil;
    [navBar removeFromSuperview];
    [navBar release];
    navBar = nil;
    [self dismissViewControllerAnimated:YES completion:nil];
    _prevLayer = nil;
    _session = nil;
    resultString = nil;
}

// device will / did rotate
- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    
    // Place code here which is performed before the rotation animation starts.
    
    [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext> context)
    {
        // Perform this code here during rotation animation
        
    } completion:^(id<UIViewControllerTransitionCoordinatorContext> context)
    {
                    
        // rotation finished, resize preview layer
        _prevLayer.frame = self.view.bounds;
        // rotate camera based on new orientation
        _prevLayer.connection.videoOrientation = [self videoOrientationFromCurrentDeviceOrientation];
        
        CGRect sbFrame = [[UIApplication sharedApplication] statusBarFrame];
        int ofs = sbFrame.size.height;
        navBar.frame = CGRectMake(0, ofs, self.view.frame.size.width, 44);

    }];
}

- (AVCaptureVideoOrientation) videoOrientationFromCurrentDeviceOrientation {
    UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];

    if (orientation == UIDeviceOrientationPortraitUpsideDown)
        return AVCaptureVideoOrientationPortraitUpsideDown;
    else if(orientation == UIInterfaceOrientationPortrait)
         return AVCaptureVideoOrientationPortrait;
    else if(orientation == UIInterfaceOrientationLandscapeLeft)
        return AVCaptureVideoOrientationLandscapeLeft;
    else if(orientation == UIInterfaceOrientationLandscapeRight)
        return AVCaptureVideoOrientationLandscapeRight;

    return AVCaptureVideoOrientationPortrait;
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    NSString *detectionString = nil;
    NSArray *barCodeTypes = @[AVMetadataObjectTypeUPCECode, AVMetadataObjectTypeCode39Code, AVMetadataObjectTypeCode39Mod43Code,
            AVMetadataObjectTypeEAN13Code, AVMetadataObjectTypeEAN8Code, AVMetadataObjectTypeCode93Code, AVMetadataObjectTypeCode128Code,
            AVMetadataObjectTypePDF417Code, AVMetadataObjectTypeQRCode, AVMetadataObjectTypeAztecCode];

    for (AVMetadataObject *metadata in metadataObjects) {
        for (NSString *type in barCodeTypes) {
            if ([metadata.type isEqualToString:type])
            {
                detectionString = [(AVMetadataMachineReadableCodeObject *)metadata stringValue];
                break;
            }
        }

        if (detectionString != nil)
        {
            break;
        }
        else
        {
            AttachLog(@"String: none");
            NSString *result = nil;
            sendScanResult(result);
        }
    }

    if (detectionString != nil)
    {
        AttachLog(@"String: %@", detectionString);
        if ([resultString length] != 0) {
            if([_session isRunning])
            {
                [_session stopRunning];
            }
            [_session removeInput:_input];
            [_session removeOutput:_output];
            UIAlertController *toast =[UIAlertController alertControllerWithTitle:nil 
                message:[NSString stringWithFormat:@"%@: %@",resultString, detectionString] 
                preferredStyle:UIAlertControllerStyleAlert];
            [self presentViewController:toast animated:YES completion:nil];

            int duration = 2; // in seconds
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, duration * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
                [toast dismissViewControllerAnimated:YES completion:^{
                    sendScanResult(detectionString);
                    [self end];
                }];
            });
        } else {
            sendScanResult(detectionString);
            [self end];
        }
    }

}
@end
