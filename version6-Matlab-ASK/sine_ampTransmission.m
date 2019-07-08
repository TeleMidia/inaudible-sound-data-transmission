pause(0.150)

f=20000;
f2=20150;
f3=20300;
f4=20450;
f5=20600;
f6=20750;
f7=20900;
f8=21050;
f9=21200;
f10=21350;
f11=21500;
f12=21650;
f13=19500;
f14=19700;
f15=19850;
fs=44100;
samplePF=40;

sine = dsp.SineWave('Frequency',f,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine2 = dsp.SineWave('Frequency',f2,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine3 = dsp.SineWave('Frequency',f3,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine4 = dsp.SineWave('Frequency',f4,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine5 = dsp.SineWave('Frequency',f5,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine6 = dsp.SineWave('Frequency',f6,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine7 = dsp.SineWave('Frequency',f7,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine8 = dsp.SineWave('Frequency',f8,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine9 = dsp.SineWave('Frequency',f9,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine10 = dsp.SineWave('Frequency',f10,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine11 = dsp.SineWave('Frequency',f11,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine12 = dsp.SineWave('Frequency',f12,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine13 = dsp.SineWave('Frequency',f13,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.5);
sine14 = dsp.SineWave('Frequency',f14,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.3);
sine15 = dsp.SineWave('Frequency',f15,'SampleRate',fs,'SamplesPerFrame',samplePF,'Amplitude',0.1);

sineVector = {sine, sine2, sine3, sine4, sine5, sine6, sine7, sine8, sine9, sine10, sine11, sine12, sine13, sine14, sine15};
lenVector  = length(sineVector);

sineValues = cell(1,lenVector);

Out= audioDeviceWriter('SampleRate',fs);
% plot(signal,'DisplayName','Sum of all')
% 
% for x = 1 : lenVector
%     hold on 
%     plot(sineValues{x},'DisplayName',num2str(x))
% end

tic;
while(toc < 5)
    sineValues = getSineValues(sineVector);
    
    step(Out,[sineValues{1},sineValues{2},sineValues{3},sineValues{4}]);
%     step(Out,[sineValues{4},sineValues{5},sineValues{6}]);
%     step(Out,[sineValues{7},sineValues{8},sineValues{9}]);
%     step(Out,[sineValues{10},sineValues{11},sineValues{12}]);
%     step(Out,[sineValues{13},sineValues{14},sineValues{15}]);
end

function sineValues = getSineValues (sv)
    sineValues = cell(1,length(sv));
    for x = 1 : length(sv)
        sineValues{x} = step(sv{x});
    end 
end