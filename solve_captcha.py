#!/usr/bin/env python

import argparse
import os
import sys

import cv2
import numpy as np

IMG_WIDTH = 250
IMG_HEIGHT = 50
MODEL_PATH = "captcha_model.keras"


def preprocess_image(img_path):
    """Load, resize, normalize image for model prediction."""
    img = cv2.imread(img_path)
    if img is None:
        print(f"Error: Image not found at {img_path}")
        sys.exit(1)
    img = cv2.resize(img, (IMG_WIDTH, IMG_HEIGHT))  # Resize to match training input
    img = img.astype(np.float32) / 255.0  # Normalize pixel values
    return np.expand_dims(img, axis=0)  # Add batch dimension


def index_to_char(index):
    """Convert numerical index to alphanumeric character (0-9, a-z)."""
    if index < 10:
        return str(index)
    else:
        return chr(index - 10 + ord("a"))


def predict_captcha(img_path, verbose):
    """Load image, predict captcha, and return the result."""
    img_input = preprocess_image(img_path)
    predictions = model.predict(img_input, verbose=verbose)
    predicted_chars = [index_to_char(np.argmax(pred)) for pred in predictions]
    return "".join(predicted_chars)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Predict CAPTCHA from an image.")
    parser.add_argument("--verbose", action="store_true", help="Enable verbose output.")
    parser.add_argument("image_path", help="Path to the input CAPTCHA image.")

    args = parser.parse_args()

    # Apply suppression only if verbose mode is off
    if not args.verbose:
        os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"  # Suppress TensorFlow warnings

    import tensorflow as tf  # Import tensorflow here to have an ability to suppress warnings

    if not args.image_path:
        print(f"Usage: {sys.argv[0]} [--verbose] <image_path>")
        sys.exit(1)

    model = tf.keras.models.load_model(MODEL_PATH)
    predicted_text = predict_captcha(args.image_path, verbose=1 if args.verbose else 0)
    print(predicted_text)
