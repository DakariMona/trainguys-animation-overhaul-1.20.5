package com.trainguy9512.animationoverhaul.animation.animator;

import com.trainguy9512.animationoverhaul.animation.animator.entity.LivingEntityJointAnimator;
import com.trainguy9512.animationoverhaul.animation.data.AnimationPoseSamplerKey;
import com.trainguy9512.animationoverhaul.animation.data.AnimationVariableKey;
import com.trainguy9512.animationoverhaul.animation.pose.AnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.BakedAnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.sample.*;
import com.trainguy9512.animationoverhaul.util.animation.JointSkeleton;
import com.trainguy9512.animationoverhaul.animation.data.AnimationDataContainer;
import com.trainguy9512.animationoverhaul.animation.data.TimelineGroupData;
import com.trainguy9512.animationoverhaul.util.time.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FirstPersonPlayerJointAnimator extends LivingEntityJointAnimator<LocalPlayer, PlayerModel<LocalPlayer>, FirstPersonPlayerJointAnimator.FPPlayerLocators> {

    public static FirstPersonPlayerJointAnimator INSTANCE = new FirstPersonPlayerJointAnimator();

    public AnimationDataContainer localAnimationDataContainer = new AnimationDataContainer();
    public BakedAnimationPose<FPPlayerLocators> localBakedPose;


    public enum FPPlayerLocators {
        root,
        camera,
        armBuffer,
        rightArmBuffer,
        leftArmBuffer,
        rightArm,
        leftArm,
        rightHand,
        leftHand;

        public static final FPPlayerLocators[] arms = new FPPlayerLocators[] {
                rightArm,
                leftArm,
                rightArmBuffer,
                leftArmBuffer,
                rightHand,
                leftHand
        };

        public static final FPPlayerLocators[] armBufferLocators = new FPPlayerLocators[] {
                rightArmBuffer,
                leftArmBuffer
        };

        public static final FPPlayerLocators[] armPoseLocators = new FPPlayerLocators[] {
                rightArm,
                leftArm
        };

        public static final FPPlayerLocators[] handLocators = new FPPlayerLocators[] {
                rightHand,
                leftHand
        };
    }


    public static final ResourceLocation ANIMATION_FP_PLAYER_IDLE = TimelineGroupData.getNativeResourceLocation(TimelineGroupData.FIRST_PERSON_PLAYER_KEY, "fp_player_idle");


    public static final AnimationVariableKey<Float> TIME_TEST = AnimationVariableKey.of(() -> 0F).setIdentifier("time_test").build();



    public static final AnimationVariableKey<Vector3f> CAMERA_ROTATION = AnimationVariableKey.of(() -> new Vector3f(0, 0, 0)).setIdentifier("camera_rotation").build();
    public static final AnimationVariableKey<Vector3f> DAMPENED_CAMERA_ROTATION = AnimationVariableKey.of(() -> new Vector3f(0, 0, 0)).setIdentifier("dampened_camera_rotation").build();
    public static final AnimationVariableKey<ItemStack> MAIN_HAND_ITEM = AnimationVariableKey.of(() -> ItemStack.EMPTY).setIdentifier("main_hand_item_stack").build();
    public static final AnimationVariableKey<Boolean> IS_ATTACKING = AnimationVariableKey.of(() -> false).setIdentifier("is_attacking").build();
    public static final AnimationVariableKey<Boolean> IS_USING_ITEM = AnimationVariableKey.of(() -> false).setIdentifier("is_using_item").build();
    public static final AnimationVariableKey<Boolean> IS_MINING = AnimationVariableKey.of(() -> false).setIdentifier("is_mining").build();
    public static final AnimationVariableKey<Boolean> IS_FALLING = AnimationVariableKey.of(() -> false).setIdentifier("is_falling").build();
    public static final AnimationVariableKey<Boolean> IS_JUMPING = AnimationVariableKey.of(() -> false).setIdentifier("is_jumping").build();
    public static final AnimationVariableKey<Float> WALK_SPEED = AnimationVariableKey.of(() -> 0f).setIdentifier("walk_speed").build();

    public static final AnimationPoseSamplerKey<AnimationStateMachine<TestStates>> TEST_STATE_MACHINE = AnimationPoseSamplerKey.of(() -> AnimationStateMachine.of("test_state_machine", TestStates.values())
            .addStateTransition(TestStates.IDLE, TestStates.MOVING, AnimationStateMachine.StateTransition.of(
                            animationDataContainer -> animationDataContainer.getAnimationVariable(WALK_SPEED).get() > 0.1F)
                    .setTransitionTime(5)
                    .setEasing(Easing.CubicBezier.bezierInOutSine())
                    .build())
            .addStateTransition(TestStates.MOVING, TestStates.IDLE, AnimationStateMachine.StateTransition.of(
                            animationDataContainer -> animationDataContainer.getAnimationVariable(WALK_SPEED).get() < 0.1F)
                    .setTransitionTime(10)
                    .setEasing(Easing.CubicBezier.bezierOutSine())
                    .build())
            .build()).build();

    public static final AnimationPoseSamplerKey<AnimationSequencePlayer> IDLE_SEQUENCE_PLAYER = AnimationPoseSamplerKey.of(() -> AnimationSequencePlayer.of(ANIMATION_FP_PLAYER_IDLE)
            .setPlayRate(0)
            .setStartTime(0)
            .build()).setIdentifier("idle_sequence_player").build();
    public static final AnimationPoseSamplerKey<AnimationSequencePlayer> IDLE_SEQUENCE_PLAYER_ALT = AnimationPoseSamplerKey.of(() -> AnimationSequencePlayer.of(ANIMATION_FP_PLAYER_IDLE)
            .setPlayRate(1)
            .setStartTime(20)
            .addProgressTimeOnActiveStates(TEST_STATE_MACHINE, TestStates.MOVING)
            .build()).setIdentifier("idle_sequence_player").build();


    public enum TestStates implements AnimationStateMachine.StateEnum {
        IDLE {
            @Override
            public <L extends Enum<L>> BiFunction<AnimationDataContainer, JointSkeleton<L>, AnimationPose<L>> getStatePose() {
                return (animationDataContainer, fpPlayerLocatorsJointSkeleton) -> animationDataContainer.getPoseSampler(IDLE_SEQUENCE_PLAYER).sample(fpPlayerLocatorsJointSkeleton);
            }
        },
        MOVING {
            @Override
            public <L extends Enum<L>> BiFunction<AnimationDataContainer, JointSkeleton<L>, AnimationPose<L>> getStatePose() {
                return (animationDataContainer, fpPlayerLocatorsJointSkeleton) -> animationDataContainer.getPoseSampler(IDLE_SEQUENCE_PLAYER_ALT).sample(fpPlayerLocatorsJointSkeleton);
            }
        }
    }







    public FirstPersonPlayerJointAnimator(){
        super();
    }

    protected JointSkeleton<FPPlayerLocators> buildRig() {
        return JointSkeleton.of(FPPlayerLocators.root)
                .addChildLocator(FPPlayerLocators.camera)
                .addChildLocator(FPPlayerLocators.armBuffer)
                .addChildLocator(FPPlayerLocators.leftArmBuffer, FPPlayerLocators.armBuffer)
                .addChildLocator(FPPlayerLocators.rightArmBuffer, FPPlayerLocators.armBuffer)
                .addChildLocator(FPPlayerLocators.leftArm, FPPlayerLocators.leftArmBuffer)
                .addChildLocator(FPPlayerLocators.rightArm, FPPlayerLocators.rightArmBuffer)
                .addChildLocator(FPPlayerLocators.leftHand, FPPlayerLocators.leftArm)
                .addChildLocator(FPPlayerLocators.rightHand, FPPlayerLocators.rightArm)
                .setLocatorDefaultPose(FPPlayerLocators.leftHand, PartPose.offsetAndRotation(1, 10, -2, -Mth.HALF_PI, 0, Mth.PI))
                .setLocatorDefaultPose(FPPlayerLocators.rightHand, PartPose.offsetAndRotation(-1, 10, -2, -Mth.HALF_PI, 0, Mth.PI))
                .setLocatorMirror(FPPlayerLocators.rightArm, FPPlayerLocators.leftArm)
                .setLocatorMirror(FPPlayerLocators.rightHand, FPPlayerLocators.leftHand);

    }

    @Override
    public AnimationPose<FPPlayerLocators> calculatePose(LocalPlayer localPlayer, AnimationDataContainer animationDataContainer) {
        // Update main hand item based on the anim notify

        animationDataContainer.getAnimationVariable(MAIN_HAND_ITEM).set(localPlayer.getMainHandItem().copy());
        //setEntityAnimationVariable(MAIN_HAND_ITEM, this.livingEntity.getMainHandItem().copy());

        AnimationPose<FPPlayerLocators> pose = animationDataContainer.getPoseSampler(TEST_STATE_MACHINE).sample(this.getJointSkeleton());



        pose = dampenArmRotation(pose, animationDataContainer);


        Vector3f rotation = new Vector3f(Mth.sin(animationDataContainer.getAnimationVariable(TIME_TEST).get() * 0.2F) * Mth.HALF_PI * 0.7f, 0, 0);
        //Vector3f translation = new Vector3f(Mth.sin(getEntityAnimationVariable(TIME_TEST) * 1.3F) * 3F, 0, 0);
        //pose.translateJoint(FPPlayerLocators.rightArm, translation, AnimationPose.TransformSpace.ENTITY, false);
        //pose.rotateJoint(FPPlayerLocators.rightArm, rotation, AnimationPose.TransformSpace.ENTITY, false);


        return pose;
    }

    /*
    Get the pose with the added dampened camera rotation
     */
    private AnimationPose<FPPlayerLocators> dampenArmRotation(AnimationPose<FPPlayerLocators> pose, AnimationDataContainer animationDataContainer){
        Vector3f cameraRotation = animationDataContainer.getAnimationVariable(CAMERA_ROTATION).get();
        Vector3f dampenedCameraRotation = animationDataContainer.getAnimationVariable(DAMPENED_CAMERA_ROTATION).get();

        Vector3f cameraDampWeight = new Vector3f(0.6F, 0.3F, 0.1F);

        pose.setJointPose(
                FPPlayerLocators.armBuffer,
                pose.getJointPoseCopy(FPPlayerLocators.armBuffer).rotate(
                        new Vector3f(
                                (dampenedCameraRotation.x() - cameraRotation.x()) * (cameraDampWeight.x() * 0.01F),
                                (dampenedCameraRotation.y() - cameraRotation.y()) * (cameraDampWeight.y() * 0.01F),
                                (dampenedCameraRotation.z() - cameraRotation.z()) * (cameraDampWeight.z() * 0.01F)
                        ),
                        AnimationPose.TransformSpace.ENTITY
                ));
        return pose;
    }


    @Override
    public void tick(LocalPlayer localPlayer, AnimationDataContainer animationDataContainer){



        animationDataContainer.getAnimationVariable(WALK_SPEED).set(this.getWalkAnimationSpeed(localPlayer));
        animationDataContainer.getAnimationVariable(TIME_TEST).set(animationDataContainer.getAnimationVariable(TIME_TEST).get() + 1);




        //Tick the dampened camera rotation.
        Vector3f dampenSpeed = new Vector3f(0.5F, 0.5F, 0.2F);

        // First, set the target camera rotation from the living entity.
        Vector3f targetRotation = new Vector3f(localPlayer.getXRot(), localPlayer.getYRot(), localPlayer.getYRot());
        animationDataContainer.getAnimationVariable(CAMERA_ROTATION).set(targetRotation);


        Vector3f dampenedCameraRotation = animationDataContainer.getAnimationVariable(DAMPENED_CAMERA_ROTATION).get();

        // If the dampened camera rotation is 0 (which is what it is upon initialization), set it to the target
        if(dampenedCameraRotation.x() == 0F && dampenedCameraRotation.y() == 0F){
            dampenedCameraRotation = targetRotation;
        } else {
            // Lerp the dampened camera rotation towards the normal camera rotation
            dampenedCameraRotation.set(
                    Mth.lerp(dampenSpeed.x(), dampenedCameraRotation.x(), targetRotation.x()),
                    Mth.lerp(dampenSpeed.y(), dampenedCameraRotation.y(), targetRotation.y()),
                    Mth.lerp(dampenSpeed.z(), dampenedCameraRotation.z(), targetRotation.z())
            );
            //dampenedCameraRotation.lerp(targetRotation, 0.5F);
        }
        animationDataContainer.getAnimationVariable(DAMPENED_CAMERA_ROTATION).set(dampenedCameraRotation);

    }


    //TODO: Move this elsewhere

    public void tickExternal(){
        LocalPlayer player = Minecraft.getInstance().player;
        AnimationDataContainer animationDataContainer = this.localAnimationDataContainer;

        this.tick(player, animationDataContainer);
        animationDataContainer.tickAllPoseSamplers();

        if(this.localBakedPose == null){
            this.localBakedPose = new BakedAnimationPose<>();
            this.localBakedPose.setPose(AnimationPose.of(this.jointSkeleton));
        }
        if(!this.localBakedPose.hasPose){
            this.localBakedPose.setPose(AnimationPose.of(this.jointSkeleton));
            this.localBakedPose.hasPose = true;
        }
        this.localBakedPose.pushToOld();

        AnimationPose<FPPlayerLocators> animationPose = this.calculatePose(player, animationDataContainer);
        if (animationPose == null){
            animationPose = AnimationPose.of(this.jointSkeleton);
        }
        animationPose.applyDefaultPoseOffset();




        this.localBakedPose.setPose(new AnimationPose<>(animationPose));
    }

    private boolean compareVariableItemStackWithEntityItemStack(AnimationVariableKey<ItemStack> itemStackDataKey, ItemStack entityItemStack, AnimationDataContainer animationDataContainer){
        ItemStack currentItemStack = animationDataContainer.getAnimationVariable(itemStackDataKey).get();
        if(currentItemStack.getItem() != null && entityItemStack.getItem() == null || currentItemStack.getItem() == null && entityItemStack.getItem() != null) {
            return true;
        }
        return currentItemStack.getItem() != entityItemStack.getItem();
    }
}
