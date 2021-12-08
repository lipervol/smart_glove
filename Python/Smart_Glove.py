#coding:UTF-8
import tensorflow as tf
import numpy as np
import pathlib
from tensorflow.keras.layers import *
from tensorflow.keras.models import *
import pandas as pd
import matplotlib.pyplot as plt
import os

time_len=256 #最大时间步长度
data_len=18 # 数据长度

#CSV文件处理
csv=pd.read_csv("Data.csv",header=None)
data=[]
label=[]
seg=0
for i in range(csv.shape[0]):
    if(csv.iloc[i][0] != 0.):
        label.append(int(csv.iloc[i][1]))
        frame=csv.iloc[seg:i].values
        data.append(frame[:,1:].tolist())
        seg=i+1

#对data中长度不一的数据进行补0到最大时间步长度
data=tf.keras.preprocessing.sequence.pad_sequences(data,padding="post",dtype=float,maxlen=time_len)

#打乱顺序
np.random.seed(7)
np.random.shuffle(data)
np.random.seed(7)
np.random.shuffle(label)
tf.random.set_seed(7)

#分割训练集和数据集
div=int(0.2*len(data))
x_train=data[:-div]
y_train=label[:-div]
x_test=data[-div:]
y_test=label[-div:]

#转换为numpy格式
x_train=np.array(x_train)
y_train=np.array(y_train)
x_test=np.array(x_test)
y_test=np.array(y_test)

#模型结构
model = Sequential([Masking(mask_value=0.,input_shape=(time_len,data_len)),#Masking层忽略全为0的时间步
                    SimpleRNN(32,return_sequences=True),
                    Dropout(0.2),
                    SimpleRNN(64,return_sequences=False),
                    Dropout(0.2),
                    Dense(32,activation="relu"),
                    Dense(10,activation="softmax")])

#反向传递和误差计算
model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=False),
              metrics=['sparse_categorical_accuracy'])

#断点续训
save_path="./checkpoint/save"
if os.path.exists(save_path+'.index'):
    model.load_weights(save_path)

cp_callback=tf.keras.callbacks.ModelCheckpoint(filepath=save_path,
                                              save_weights_only=True,
                                              save_best_only=True)

#开始训练
history=model.fit(x_train,y_train,batch_size=16,epochs=50,validation_data=(x_test,y_test),validation_freq=1,callbacks=[cp_callback])

#保存为tflite模型文件，为植入android做准备
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
tflite_model_file = pathlib.Path('model.tflite')
tflite_model_file.write_bytes(tflite_model)

#训练过程正确率和误差变化绘图
acc = history.history['sparse_categorical_accuracy']
val_acc = history.history['val_sparse_categorical_accuracy']
loss = history.history['loss']
val_loss = history.history['val_loss']

plt.subplot(1, 2, 1)
plt.plot(acc, label='Training Accuracy')
plt.plot(val_acc, label='Validation Accuracy')
plt.title('Training and Validation Accuracy')
plt.legend()

plt.subplot(1, 2, 2)
plt.plot(loss, label='Training Loss')
plt.plot(val_loss, label='Validation Loss')
plt.title('Training and Validation Loss')
plt.legend()
plt.show()