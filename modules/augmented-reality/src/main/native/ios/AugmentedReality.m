/*
 * Copyright (c) 2018, 2019, Gluon
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
#include "AugmentedReality.h"

// JNIEnv *env = NULL;

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_AugmentedReality(JavaVM *vm, void *reserved)
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

static int AugmentedRealityInited = 0;

// AugmentedReality
jclass mat_jAugmentedRealityServiceClass;
jmethodID mat_jAugmentedRealityService_notifyCancel = 0;

API_AVAILABLE(ios(11.3))
AugmentedReality *_ar;

BOOL enableDebugAugmentedReality;

JNIEXPORT jint JNICALL Java_com_gluonhq_attach_ar_impl_IOSAugmentedRealityService_initAR
(JNIEnv *myenv, jclass jClass)
{
    if (AugmentedRealityInited)
    {
        return 0;
    }
    AugmentedRealityInited = 1;

    mat_jAugmentedRealityServiceClass = (*myenv)->NewGlobalRef(myenv, (*myenv)->FindClass(myenv, "com/gluonhq/attach/ar/impl/IOSAugmentedRealityService"));
    mat_jAugmentedRealityService_notifyCancel = (*myenv)->GetStaticMethodID(myenv, mat_jAugmentedRealityServiceClass, "notifyCancel", "()V");

    AttachLog(@"Init AugmentedReality");
    if (@available(iOS 11.0, *)) { // First of all, ARConfiguration requires iOS 11.0+
        if (ARConfiguration.isSupported) { // Then, AR requires chip A9+ that supports AR
            if (@available(iOS 11.3, *)) { // this app uses APIs that require iOS 11.3+
                AttachLog(@"ARKit is supported and iOS is at least 11.3");
                _ar = [[AugmentedReality alloc] init];
                return 2;
            } else {
                AttachLog(@"ARKit requires at least 11.3. Please update your device");
                return 1;
            }
        }
    }
    AttachLog(@"ARKit is not supported");
    return 0;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ar_impl_IOSAugmentedRealityService_enableDebugAR
(JNIEnv *env, jclass jClass)
{
    enableDebugAugmentedReality = YES;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ar_impl_IOSAugmentedRealityService_showNativeAR
(JNIEnv *env, jclass jClass)
{
    if (@available(iOS 11.3, *)) {
        if (_ar) 
        {
            [_ar showAR];
        }
    }
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ar_impl_IOSAugmentedRealityService_setARModel
(JNIEnv *env, jclass jClass, jstring jObjFileName, jdouble scale)
{
    const jchar *charsObjFileName = (*env)->GetStringChars(env, jObjFileName, NULL);
    NSString *objFileName = [NSString stringWithCharacters:(UniChar *)charsObjFileName length:(*env)->GetStringLength(env, jObjFileName)];
    (*env)->ReleaseStringChars(env, jObjFileName, charsObjFileName);
    
    if (@available(iOS 11.3, *)) {
        if (_ar) 
        {
            [_ar setARModel:objFileName scale:scale];
        }
    }
    return;
}

void sendCancelled() {
    AttachLog(@"Sending cancel action");
    (*env)->CallStaticVoidMethod(env, mat_jAugmentedRealityServiceClass, mat_jAugmentedRealityService_notifyCancel);
}

@implementation AugmentedReality 

#pragma mark - Overriding UIViewController

double modelScale = 1.0;

- (BOOL)prefersStatusBarHidden {
  return YES;
}

- (void)setARModel:(NSString *)fileName scale:(double)scale
{
    self.modelFileName = fileName;
    modelScale = scale;
    [self logMessage:@"Set ARModel: %@ %.2f", self.modelFileName, modelScale];
}

- (void)showAR
{
    [self logMessage:@"showing AR"];

    if(![[UIApplication sharedApplication] keyWindow])
    {
        AttachLog(@"key window was nil");
        return;
    }
    UIWindow* window = [UIApplication sharedApplication].keyWindow;
    
    NSArray *views = [window subviews];
    if(![views count]) {
        AttachLog(@"views size was 0");
        return;
    }

    UIView *_currentView = views[0];

    UIViewController *rootViewController = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    if(!rootViewController)
    {
        AttachLog(@"rootViewController was nil");
        return;
    }

    // Stop the screen from dimming while we are using the app
    [UIApplication.sharedApplication setIdleTimerDisabled:YES];

    [self logMessage:@"adding sceneView"];
    self.sceneView = [[ARSCNView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.sceneView.contentMode = UIViewContentModeScaleToFill;
    self.sceneView.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
    self.sceneView.multipleTouchEnabled = YES;
    self.sceneView.antialiasingMode = SCNAntialiasingModeMultisampling4X;
    self.sceneView.autoenablesDefaultLighting = YES;
    self.sceneView.automaticallyUpdatesLighting = NO;

    self.sceneView.delegate = self;
    [self logMessage:@"got sceneView %@", self.sceneView];
    if (enableDebugAugmentedReality) {
        self.sceneView.showsStatistics = YES;
        self.sceneView.debugOptions = ARSCNDebugOptionShowFeaturePoints | ARSCNDebugOptionShowWorldOrigin;
    }
    self.sceneView.userInteractionEnabled = YES;
    
    [self logMessage:@"SceneView: %@", self.sceneView];
    
    [self logMessage:@"adding scene"];
    SCNScene *scene = [[SCNScene alloc] init];
    self.sceneView.scene = scene;

    [self logMessage:@"adding subView"];
    [_currentView addSubview:self.sceneView];
    [_currentView bringSubviewToFront:self.sceneView];

    
    ARWorldTrackingConfiguration *configuration = [ARWorldTrackingConfiguration new];
    [configuration setWorldAlignment:ARWorldAlignmentGravity];
    [configuration setPlaneDetection:ARPlaneDetectionHorizontal];
    configuration.lightEstimationEnabled = YES;
    
    [self logMessage:@"run sceneView"];
    self.sceneView.session = [[ARSession alloc] init];
    [self.sceneView.session runWithConfiguration:configuration];
    self.sceneView.session.delegate = self;
    
    [self logMessage:@"***** Running %@", self.sceneView.session];
    
    self.cancelButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.cancelButton setTitle:@"CANCEL" forState:UIControlStateNormal];
    [self.cancelButton addTarget:self action:@selector(cancelButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    self.cancelButton.frame = CGRectMake(16.0, 44.0, 100.0, 30.0);
    [self.sceneView addSubview:self.cancelButton];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self startObserver];
    });

    UITapGestureRecognizer *singleFingerTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleTap:)];
    [self.sceneView addGestureRecognizer:singleFingerTap];
}

- (void)hideAR {
    [UIApplication.sharedApplication setIdleTimerDisabled:NO];
    if (! self.sceneView.scene) {
        [self logMessage:@"Remove nodes"];    
        for (SCNNode *childNode in [[self.sceneView.scene rootNode] childNodes]) {
            [childNode removeFromParentNode];
        }
    }
    [self logMessage:@"Stop session"];    
    [self.sceneView.session pause];
    
    [self logMessage:@"Remove sceneView"];    
    [self.sceneView removeFromSuperview];
    [self stopObserver];
}

#pragma mark - ARSCNViewDelegate

- (void) renderer:(id<SCNSceneRenderer>)renderer didAddNode:(nonnull SCNNode *)node forAnchor:(nonnull ARAnchor *)anchor {
    if (enableDebugAugmentedReality && [anchor isKindOfClass:[ARPlaneAnchor class]]) {
        [self logMessage:@"didAddNode: add plane"];   
        ARPlaneAnchor *planeAnchor = (ARPlaneAnchor *)anchor;

        CGFloat width = planeAnchor.extent.x;
        CGFloat height = planeAnchor.extent.z;
        SCNPlane *plane = [SCNPlane planeWithWidth:width height:height];

        plane.materials.firstObject.diffuse.contents =
            [UIColor colorWithRed:0.0f green:0.0f blue:1.0f alpha:0.3f];

        SCNNode *planeNode = [SCNNode nodeWithGeometry:plane];

        CGFloat x = planeAnchor.center.x;
        CGFloat y = planeAnchor.center.y;
        CGFloat z = planeAnchor.center.z;
        planeNode.position = SCNVector3Make(x, y, z);
        planeNode.eulerAngles = SCNVector3Make(-M_PI / 2, 0, 0);

        [node addChildNode:planeNode];
    }
}

- (void)renderer:(id<SCNSceneRenderer>)renderer didUpdateNode:(SCNNode *)node forAnchor:(ARAnchor *)anchor {
    if (enableDebugAugmentedReality && [anchor isKindOfClass:[ARPlaneAnchor class]]) {
        ARPlaneAnchor *planeAnchor = (ARPlaneAnchor *)anchor;

        SCNNode *planeNode = node.childNodes.firstObject;
        SCNPlane *plane = (SCNPlane *)planeNode.geometry;

        CGFloat width = planeAnchor.extent.x;
        CGFloat height = planeAnchor.extent.z;
        plane.width = width;
        plane.height = height;

        CGFloat x = planeAnchor.center.x;
        CGFloat y = planeAnchor.center.y;
        CGFloat z = planeAnchor.center.z;
        planeNode.position = SCNVector3Make(x, y, z);
    }
}

- (void)renderer:(id<SCNSceneRenderer>)renderer didRemoveNode:(SCNNode *)node forAnchor:(ARAnchor *)anchor {
    if ([anchor isKindOfClass:[ARPlaneAnchor class]]) {
        [self logMessage:@"didRemoveNode: remove plane"];   
        SCNNode *planeNode = node.childNodes.firstObject;
        [planeNode removeFromParentNode];
    }
}

- (void)renderer:(id <SCNSceneRenderer>)renderer updateAtTime:(NSTimeInterval)time {
    ARLightEstimate *estimate = self.sceneView.session.currentFrame.lightEstimate;
    if (! estimate) {
        return;
    }
    //AttachLog(@"light estimate: %f", estimate.ambientIntensity);

    CGFloat intensity = estimate.ambientIntensity / 1000.0;
    self.sceneView.scene.lightingEnvironment.intensity = intensity;
}

- (void)session:(ARSession *)session didFailWithError:(NSError *)error {
    // Present an error message to the user
    AttachLog(@"Session error: %@", error);
}

- (void)sessionWasInterrupted:(ARSession *)session {
    // Inform the user that the session has been interrupted, for example, by presenting an overlay
    AttachLog(@"session was interrupted: %@", session);
}

- (void)sessionInterruptionEnded:(ARSession *)session {
    // Reset tracking and/or remove existing anchors if consistent tracking is required
    AttachLog(@"session interruption ended: %@", session);
}

#pragma mark - ARSessionDelegate

- (void)session:(ARSession *)session didUpdateFrame:(ARFrame *)frame 
{
}

# pragma mark - Actions

- (void)cancelButtonPressed:(UIButton*)sender {
    [self logMessage:@"Cancel AR session"];    
    [self hideAR];
    sendCancelled();
}

//The event handling method
- (void)handleSingleTap:(UITapGestureRecognizer *)recognizer
{
    CGPoint touchLocation = [recognizer locationInView:[recognizer.view superview]];
    [self logMessage:@"TapGesture: %@", NSStringFromCGPoint(touchLocation)];
  
    NSArray *hitTestResults = [self.sceneView hitTest:touchLocation types:ARHitTestResultTypeExistingPlane |
                          ARHitTestResultTypeExistingPlaneUsingExtent |
                          ARHitTestResultTypeEstimatedHorizontalPlane];
    [self logMessage:@"HitTestResults: %@", hitTestResults];    
    if (hitTestResults.count > 0) {
        ARHitTestResult *result = [hitTestResults firstObject];
        [self logMessage:@"Result %@", result];      

        SCNNode *model = [[SCNNode new] autorelease];
        // Create a new scene
        if ([self.modelFileName length] > 0) {
            SCNScene *scene = [SCNScene sceneNamed:self.modelFileName];
            [self logMessage:@"Adding new scene %@", scene];
            for (SCNNode *childNode in [[scene rootNode] childNodes]) {
                [model addChildNode:childNode];
            }
        } else {
            AttachLog(@"No model was set. Use AugmentedRealityService::setModel");
        }
        [self logMessage:@"node: %@", model];
        SCNMatrix4 sc = SCNMatrix4MakeScale(modelScale, modelScale, modelScale);
        model.transform = SCNMatrix4Mult(SCNMatrix4FromMat4(result.worldTransform), sc);
        [self.sceneView.scene.rootNode addChildNode:model];
    }    
}

- (void) startObserver 
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(OrientationDidChange:) name:UIDeviceOrientationDidChangeNotification object:nil];
}

- (void) stopObserver 
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
}

-(void)OrientationDidChange:(NSNotification*)notification
{
    [self logMessage:@"adjustiong sceneView frame"];
    self.sceneView.frame = [[UIScreen mainScreen] bounds];
}

- (void) logMessage:(NSString *)format, ...;
{
    if (debugAttach)
    {
        va_list args;
        va_start(args, format);
        NSString* formattedMessage = [[NSString alloc] initWithFormat: format arguments: args];
        AttachLog([@"[Debug] " stringByAppendingString:formattedMessage]);
        va_end(args);
        [formattedMessage release];
    }
}
@end
